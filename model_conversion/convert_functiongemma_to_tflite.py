#!/usr/bin/env python3
"""
Convert FunctionGemma model from HuggingFace to TFLite for Android.

This script:
1. Downloads google/functiongemma-270m-it from HuggingFace
2. Converts to ONNX format
3. Converts ONNX to TensorFlow
4. Converts TensorFlow to TFLite with INT8 quantization
5. Exports tokenizer for Android

Requirements:
    pip install -r requirements.txt

Usage:
    python convert_functiongemma_to_tflite.py

Output:
    - outputs/functiongemma.tflite (quantized model ~70-135MB)
    - outputs/tokenizer.json (tokenizer config)
    - outputs/vocab.txt (vocabulary)
"""

import os
import sys
import json
import shutil
import argparse
from pathlib import Path

def check_dependencies():
    """Check and install required dependencies."""
    required = [
        'torch',
        'transformers',
        'optimum',
        'onnx',
        'onnxruntime',
        'tensorflow',
        'numpy',
        'sentencepiece'
    ]

    missing = []
    for pkg in required:
        try:
            __import__(pkg)
        except ImportError:
            missing.append(pkg)

    if missing:
        print(f"Missing packages: {missing}")
        print("Installing missing packages...")
        import subprocess
        subprocess.check_call([sys.executable, '-m', 'pip', 'install'] + missing)
        print("Packages installed. Please restart the script.")
        sys.exit(0)

# Check dependencies first
check_dependencies()

import torch
import numpy as np
from transformers import AutoTokenizer, AutoModelForCausalLM
import tensorflow as tf

# Constants
MODEL_NAME = "google/functiongemma-270m-it"
OUTPUT_DIR = Path("outputs")
ONNX_DIR = OUTPUT_DIR / "onnx"
TF_DIR = OUTPUT_DIR / "tensorflow"
TFLITE_PATH = OUTPUT_DIR / "functiongemma.tflite"
TFLITE_QUANTIZED_PATH = OUTPUT_DIR / "functiongemma_int8.tflite"

# Model configuration for FunctionGemma 270M
MAX_SEQ_LENGTH = 512  # Reduced for mobile
BATCH_SIZE = 1


def setup_directories():
    """Create output directories."""
    OUTPUT_DIR.mkdir(exist_ok=True)
    ONNX_DIR.mkdir(exist_ok=True)
    TF_DIR.mkdir(exist_ok=True)
    print(f"Output directory: {OUTPUT_DIR.absolute()}")


def download_model():
    """Download model and tokenizer from HuggingFace."""
    print("\n" + "="*60)
    print("Step 1: Downloading FunctionGemma model from HuggingFace")
    print("="*60)

    print(f"Downloading {MODEL_NAME}...")
    print("This may take a few minutes depending on your connection...")

    # Download tokenizer
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
    print(f"Tokenizer downloaded. Vocab size: {tokenizer.vocab_size}")

    # Download model (without device_map for compatibility)
    model = AutoModelForCausalLM.from_pretrained(
        MODEL_NAME,
        torch_dtype=torch.float32,  # Use float32 for conversion
        trust_remote_code=True,
        low_cpu_mem_usage=True
    )
    model = model.cpu()  # Ensure on CPU
    print(f"Model downloaded. Parameters: {sum(p.numel() for p in model.parameters()):,}")

    return model, tokenizer


def export_tokenizer(tokenizer):
    """Export tokenizer files for Android."""
    print("\n" + "="*60)
    print("Step 2: Exporting tokenizer for Android")
    print("="*60)

    # Save tokenizer config
    tokenizer.save_pretrained(OUTPUT_DIR / "tokenizer")

    # Create simplified tokenizer config for Android
    tokenizer_config = {
        "model_type": "gemma",
        "vocab_size": tokenizer.vocab_size,
        "bos_token": tokenizer.bos_token,
        "eos_token": tokenizer.eos_token,
        "pad_token": tokenizer.pad_token,
        "unk_token": tokenizer.unk_token,
        "max_length": MAX_SEQ_LENGTH,
        "special_tokens": {
            "bos_token_id": tokenizer.bos_token_id,
            "eos_token_id": tokenizer.eos_token_id,
            "pad_token_id": tokenizer.pad_token_id,
        }
    }

    with open(OUTPUT_DIR / "tokenizer_config.json", 'w') as f:
        json.dump(tokenizer_config, f, indent=2)

    # Copy sentencepiece model if exists
    sp_model_path = Path(tokenizer.vocab_file) if hasattr(tokenizer, 'vocab_file') else None
    if sp_model_path and sp_model_path.exists():
        shutil.copy(sp_model_path, OUTPUT_DIR / "tokenizer.model")
        print(f"SentencePiece model copied: {OUTPUT_DIR / 'tokenizer.model'}")

    print(f"Tokenizer config saved: {OUTPUT_DIR / 'tokenizer_config.json'}")
    return tokenizer_config


def convert_to_onnx(model, tokenizer):
    """Convert PyTorch model to ONNX format."""
    print("\n" + "="*60)
    print("Step 3: Converting to ONNX format")
    print("="*60)

    # Use direct torch.onnx.export
    return manual_onnx_export(model, tokenizer)


def manual_onnx_export(model, tokenizer):
    """Manual ONNX export with cache handling."""
    import onnx

    model.eval()

    # Create a wrapper model that returns only logits (no cache)
    class ModelWrapper(torch.nn.Module):
        def __init__(self, model):
            super().__init__()
            self.model = model

        def forward(self, input_ids, attention_mask):
            outputs = self.model(
                input_ids=input_ids,
                attention_mask=attention_mask,
                use_cache=False,  # Disable cache for export
                return_dict=False
            )
            # Return only logits
            return outputs[0] if isinstance(outputs, tuple) else outputs.logits

    wrapped_model = ModelWrapper(model)
    wrapped_model.eval()

    # Create dummy input
    dummy_input = tokenizer(
        "Hello",
        return_tensors="pt",
        padding="max_length",
        max_length=32,
        truncation=True
    )

    input_ids = dummy_input["input_ids"]
    attention_mask = dummy_input["attention_mask"]

    # Export to ONNX
    onnx_path = ONNX_DIR / "model.onnx"

    print("Exporting to ONNX (this may take a few minutes)...")

    with torch.no_grad():
        torch.onnx.export(
            wrapped_model,
            (input_ids, attention_mask),
            str(onnx_path),
            export_params=True,
            opset_version=14,
            do_constant_folding=True,
            input_names=['input_ids', 'attention_mask'],
            output_names=['logits'],
            dynamic_axes={
                'input_ids': {0: 'batch_size', 1: 'sequence'},
                'attention_mask': {0: 'batch_size', 1: 'sequence'},
                'logits': {0: 'batch_size', 1: 'sequence'}
            }
        )

    # Verify ONNX model
    print("Verifying ONNX model...")
    onnx_model = onnx.load(str(onnx_path))
    onnx.checker.check_model(onnx_model)
    print(f"ONNX model saved: {onnx_path}")
    print(f"ONNX model size: {os.path.getsize(onnx_path) / 1024 / 1024:.2f} MB")

    return True


def convert_onnx_to_tflite():
    """Convert ONNX model to TFLite."""
    print("\n" + "="*60)
    print("Step 4: Converting ONNX to TFLite")
    print("="*60)

    try:
        # Method 1: Using onnx-tf
        import onnx
        from onnx_tf.backend import prepare

        onnx_path = ONNX_DIR / "model.onnx"
        if not onnx_path.exists():
            # Check for decoder model from optimum export
            onnx_path = ONNX_DIR / "decoder_model.onnx"

        print(f"Loading ONNX model from: {onnx_path}")
        onnx_model = onnx.load(str(onnx_path))

        print("Converting ONNX to TensorFlow...")
        tf_rep = prepare(onnx_model)
        tf_rep.export_graph(str(TF_DIR))
        print(f"TensorFlow model saved: {TF_DIR}")

        # Convert to TFLite
        print("Converting TensorFlow to TFLite...")
        converter = tf.lite.TFLiteConverter.from_saved_model(str(TF_DIR))

        # Basic conversion first
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,
            tf.lite.OpsSet.SELECT_TF_OPS
        ]
        converter._experimental_lower_tensor_list_ops = False

        tflite_model = converter.convert()

        with open(TFLITE_PATH, 'wb') as f:
            f.write(tflite_model)

        print(f"TFLite model saved: {TFLITE_PATH}")
        print(f"Model size: {os.path.getsize(TFLITE_PATH) / 1024 / 1024:.2f} MB")

        return True

    except ImportError:
        print("onnx-tf not installed. Installing...")
        import subprocess
        subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'onnx-tf'])
        return convert_onnx_to_tflite()
    except Exception as e:
        print(f"ONNX-TF conversion failed: {e}")
        print("Trying alternative conversion method...")
        return alternative_tflite_conversion()


def alternative_tflite_conversion():
    """Alternative TFLite conversion using ai-edge-torch."""
    print("\nTrying ai-edge-torch conversion...")

    try:
        import ai_edge_torch

        # Reload model
        model = AutoModelForCausalLM.from_pretrained(
            MODEL_NAME,
            torch_dtype=torch.float32,
            device_map="cpu"
        )
        tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

        # Create sample input
        sample_input = tokenizer(
            "sell 3 coca cola",
            return_tensors="pt",
            padding="max_length",
            max_length=32,
            truncation=True
        )

        # Convert using ai-edge-torch
        edge_model = ai_edge_torch.convert(
            model,
            (sample_input["input_ids"],),
        )

        edge_model.export(str(TFLITE_PATH))
        print(f"TFLite model saved: {TFLITE_PATH}")
        return True

    except ImportError:
        print("ai-edge-torch not available.")
        return create_simplified_tflite()
    except Exception as e:
        print(f"ai-edge-torch conversion failed: {e}")
        return create_simplified_tflite()


def create_simplified_tflite():
    """Create a simplified TFLite model for basic inference."""
    print("\n" + "="*60)
    print("Creating simplified TFLite model")
    print("="*60)

    # For complex transformer models, we'll create a TFLite model
    # that handles the core inference logic

    print("Note: Full LLM to TFLite conversion is complex.")
    print("Recommendation: Use MediaPipe LLM Inference API for production.")
    print("\nCreating placeholder model structure...")

    # Create a simple embedding + linear model as placeholder
    # The actual inference will use the tokenizer + simpler logic

    class SimpleModel(tf.Module):
        def __init__(self, vocab_size=256000, embed_dim=256, hidden_dim=512):
            super().__init__()
            self.embedding = tf.Variable(
                tf.random.normal([vocab_size, embed_dim], stddev=0.02),
                name='embedding'
            )
            self.dense1 = tf.Variable(
                tf.random.normal([embed_dim, hidden_dim], stddev=0.02),
                name='dense1'
            )
            self.dense2 = tf.Variable(
                tf.random.normal([hidden_dim, vocab_size], stddev=0.02),
                name='dense2'
            )

        @tf.function(input_signature=[
            tf.TensorSpec(shape=[1, None], dtype=tf.int32, name='input_ids')
        ])
        def __call__(self, input_ids):
            # Simple forward pass
            x = tf.nn.embedding_lookup(self.embedding, input_ids)
            x = tf.reduce_mean(x, axis=1)  # Pool
            x = tf.nn.relu(tf.matmul(x, self.dense1))
            logits = tf.matmul(x, self.dense2)
            return logits

    # This is a placeholder - for actual deployment, use MediaPipe
    print("\nWARNING: This creates a placeholder model.")
    print("For production, see: model_conversion/MEDIAPIPE_SETUP.md")

    return False


def quantize_tflite():
    """Apply INT8 quantization to TFLite model."""
    print("\n" + "="*60)
    print("Step 5: Applying INT8 quantization")
    print("="*60)

    if not TFLITE_PATH.exists():
        print("TFLite model not found. Skipping quantization.")
        return False

    try:
        # Load the TFLite model
        converter = tf.lite.TFLiteConverter.from_saved_model(str(TF_DIR))

        # Enable quantization
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.int8]

        # Representative dataset for calibration
        def representative_dataset():
            for _ in range(100):
                # Generate random input for calibration
                data = np.random.randint(0, 1000, size=(1, 32)).astype(np.int32)
                yield [data]

        converter.representative_dataset = representative_dataset

        # Convert with quantization
        quantized_model = converter.convert()

        with open(TFLITE_QUANTIZED_PATH, 'wb') as f:
            f.write(quantized_model)

        original_size = os.path.getsize(TFLITE_PATH) / 1024 / 1024
        quantized_size = os.path.getsize(TFLITE_QUANTIZED_PATH) / 1024 / 1024

        print(f"Original model: {original_size:.2f} MB")
        print(f"Quantized model: {quantized_size:.2f} MB")
        print(f"Size reduction: {(1 - quantized_size/original_size) * 100:.1f}%")

        return True

    except Exception as e:
        print(f"Quantization failed: {e}")
        return False


def create_android_assets():
    """Create Android assets directory structure."""
    print("\n" + "="*60)
    print("Step 6: Creating Android assets")
    print("="*60)

    android_assets = Path("../app/src/main/assets/functiongemma")
    android_assets.mkdir(parents=True, exist_ok=True)

    # Copy model files
    model_file = TFLITE_QUANTIZED_PATH if TFLITE_QUANTIZED_PATH.exists() else TFLITE_PATH
    if model_file.exists():
        shutil.copy(model_file, android_assets / "model.tflite")
        print(f"Model copied to: {android_assets / 'model.tflite'}")

    # Copy tokenizer files
    tokenizer_files = [
        "tokenizer_config.json",
        "tokenizer.model",
    ]

    tokenizer_dir = OUTPUT_DIR / "tokenizer"
    if tokenizer_dir.exists():
        for f in tokenizer_dir.iterdir():
            shutil.copy(f, android_assets / f.name)
            print(f"Copied: {f.name}")

    # Copy config
    if (OUTPUT_DIR / "tokenizer_config.json").exists():
        shutil.copy(OUTPUT_DIR / "tokenizer_config.json", android_assets / "config.json")

    print(f"\nAndroid assets created at: {android_assets.absolute()}")
    return android_assets


def create_mediapipe_setup_guide():
    """Create MediaPipe setup guide as alternative."""
    guide = """# MediaPipe LLM Inference Setup for FunctionGemma

Since direct TFLite conversion of large language models is complex,
Google recommends using MediaPipe LLM Inference API for Gemma models.

## Option 1: MediaPipe LLM Inference (Recommended)

### Step 1: Add dependencies

```gradle
// app/build.gradle.kts
dependencies {
    implementation("com.google.mediapipe:tasks-genai:0.10.14")
}
```

### Step 2: Download the model

Download Gemma 2B from Kaggle (requires account):
https://www.kaggle.com/models/google/gemma/frameworks/tfLite

Or use the smaller Gemma models optimized for mobile.

### Step 3: Use MediaPipe in code

```kotlin
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class MediaPipeLLMInference(context: Context) {
    private var llmInference: LlmInference? = null

    fun initialize(modelPath: String) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(512)
            .setTemperature(0.7f)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generate(prompt: String): String {
        return llmInference?.generateResponse(prompt) ?: ""
    }
}
```

## Option 2: Use Smaller Model

Consider using a smaller, fine-tuned model specifically for function calling:
- Quantized to 4-bit (~70MB)
- Optimized for mobile inference
- Custom trained on your DukaAI dataset

## Option 3: Hybrid Approach (Recommended for DukaAI)

1. Use pattern matching for common commands (fast, offline)
2. Use on-device ML for ambiguous cases
3. Optional cloud fallback for complex queries

This is already implemented in FunctionGemmaService.kt!

## Resources

- MediaPipe LLM: https://developers.google.com/mediapipe/solutions/genai/llm_inference
- Gemma on Android: https://ai.google.dev/gemma/docs/get_started/android
- TFLite for Android: https://www.tensorflow.org/lite/android
"""

    guide_path = OUTPUT_DIR / "MEDIAPIPE_SETUP.md"
    with open(guide_path, 'w') as f:
        f.write(guide)

    print(f"\nMediaPipe setup guide: {guide_path}")


def create_requirements_file():
    """Create requirements.txt for the conversion script."""
    requirements = """# Requirements for FunctionGemma to TFLite conversion
torch>=2.0.0
transformers>=4.40.0
optimum>=1.19.0
onnx>=1.15.0
onnxruntime>=1.17.0
onnx-tf>=1.10.0
tensorflow>=2.15.0
numpy>=1.24.0
sentencepiece>=0.1.99
protobuf>=3.20.0
"""

    req_path = Path("requirements.txt")
    with open(req_path, 'w') as f:
        f.write(requirements)

    print(f"Requirements file: {req_path}")


def main():
    """Main conversion pipeline."""
    print("="*60)
    print("FunctionGemma to TFLite Conversion for Android")
    print("="*60)
    print(f"Source model: {MODEL_NAME}")
    print(f"Output directory: {OUTPUT_DIR.absolute()}")
    print("="*60)

    # Setup
    setup_directories()
    create_requirements_file()

    # Download model
    model, tokenizer = download_model()

    # Export tokenizer
    export_tokenizer(tokenizer)

    # Convert to ONNX
    onnx_success = convert_to_onnx(model, tokenizer)

    if onnx_success:
        # Convert to TFLite
        tflite_success = convert_onnx_to_tflite()

        if tflite_success:
            # Quantize
            quantize_tflite()

            # Create Android assets
            create_android_assets()

    # Create MediaPipe guide as alternative
    create_mediapipe_setup_guide()

    print("\n" + "="*60)
    print("Conversion Complete!")
    print("="*60)
    print(f"\nOutput files in: {OUTPUT_DIR.absolute()}")
    print("\nNext steps:")
    print("1. Copy outputs/functiongemma*.tflite to app/src/main/assets/")
    print("2. Copy outputs/tokenizer/ files to app/src/main/assets/")
    print("3. Update FunctionGemmaInference.kt to load the model")
    print("\nAlternatively, see MEDIAPIPE_SETUP.md for MediaPipe integration")
    print("="*60)


if __name__ == "__main__":
    main()
