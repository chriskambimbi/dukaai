# FunctionGemma Training Dataset for DukaAI

## Overview

This dataset contains **2000 training examples** for fine-tuning Google's FunctionGemma model on DukaAI voice command operations. The dataset is designed specifically for Zambian retail shop management scenarios.

## Files

- `dukaai_functiongemma_training_data.jsonl` (22MB) - Training dataset in JSONL format
- `generate_training_data.py` (52KB) - Python script to regenerate or customize the dataset

## Dataset Statistics

| Metric | Count |
|--------|-------|
| **Total Examples** | 2000 |
| **Training Examples** | 1045 (52.25%) |
| **Evaluation Examples** | 955 (47.75%) |
| **Unique Tools** | 30 |
| **Operation Categories** | 21 |

## Example Breakdown by Category

| Category | Count | Example Operations |
|----------|-------|-------------------|
| Sales | 200 | "Sell 3 Coca-Cola", "Sold 5 bread" |
| Credit Sales | 150 | "John bought 2 sugar pa ng'ong'ole" |
| Stock Checks | 130 | "How many Mealie Meal in stock?" |
| Customer Balance | 130 | "How much does Mary owe?" |
| Payments | 150 | "John paid 500", "Received 200 from Sarah" |
| Add Products | 80 | "Add Kapenta at K50" |
| Stock Updates | 80 | "Add 100 Mealie Meal to stock" |
| Analytics | 150 | "Sales today", "Revenue this week" |
| Customer Management | 90 | "Add customer Moses 0977123456" |
| Low Stock Alerts | 90 | "Show me low stock items" |
| Navigation | 100 | "Go home", "Open inventory" |
| Batch Sales | 80 | "Sell Coca-Cola:2,Bread:1" |
| Product Search | 70 | "Search for Coc", "Show me beverages" |
| Top Sellers | 70 | "Top selling products today" |
| Overdue Credits | 60 | "Show me overdue credits" |
| Today's Sales | 60 | "Today's sales summary" |
| All Balances | 60 | "Who owes me money?" |
| Price Updates | 70 | "Change Sugar price to K25" |
| Out of Stock | 50 | "What's out of stock?" |
| Product Details | 70 | "Show Coca-Cola details" |
| Customer Details | 60 | "Show John's information" |

## Zambian Context

### Products Used
- **Beverages**: Coca-Cola, Fanta, Sprite, Mosi Lager, Castle Lite, Maheu, Freezit
- **Groceries**: Mealie Meal, Rice, Sugar, Salt, Cooking Oil, Kapenta, Beans
- **Snacks**: Jiggies, Munchies, Biscuits, Peanuts, Chibuku
- **Household**: Surf, Omo, Jik, Vim, Candles, Mosquito Coils
- **Bread**: Bread, Scones, Doughnuts, Buns
- **Dairy**: Milk, Yoghurt, Butter, Margarine

### Customer Names
Common Zambian names: John, Mary, James, Grace, Moses, Sarah, Mwamba, Chanda, Banda, Tembo, Mulenga, Chimwemwe, Precious, etc.

### Local Terms
- **"pa ng'ong'ole"** - Credit/buy now pay later (used in credit sale examples)
- **Kwacha (K)** - Zambian currency

## Data Format

Each line in the JSONL file is a complete JSON object with the following structure:

```json
{
  "metadata": "train" | "eval",
  "tools": [
    {
      "function": {
        "name": "tool_name",
        "description": "Tool description",
        "parameters": {
          "type": "OBJECT",
          "properties": {...},
          "required": [...]
        }
      }
    }
  ],
  "messages": [
    {
      "role": "developer",
      "content": "System prompt with date/time and context"
    },
    {
      "role": "user",
      "content": "Natural language command"
    },
    {
      "role": "assistant",
      "tool_calls": [
        {
          "function": {
            "name": "function_name",
            "arguments": {...}
          }
        }
      ]
    }
  ]
}
```

## Usage Examples

### Example 1: Simple Sale
```json
{
  "user": "Sell 3 Coca-Cola",
  "tool_call": {
    "name": "record_sale",
    "arguments": {
      "product_name": "Coca-Cola",
      "quantity": 3
    }
  }
}
```

### Example 2: Credit Sale
```json
{
  "user": "John bought 5 bread pa ng'ong'ole",
  "tool_call": {
    "name": "record_credit_sale",
    "arguments": {
      "customer_name": "John",
      "product_name": "bread",
      "quantity": 5
    }
  }
}
```

### Example 3: Payment
```json
{
  "user": "Mary paid 500",
  "tool_call": {
    "name": "record_payment",
    "arguments": {
      "customer_name": "Mary",
      "amount": 500
    }
  }
}
```

### Example 4: Stock Check
```json
{
  "user": "How many Mealie Meal in stock?",
  "tool_call": {
    "name": "check_stock",
    "arguments": {
      "product_name": "Mealie Meal"
    }
  }
}
```

## Tools Covered

The dataset includes 30 different tools across these categories:

### Product Management (10 tools)
- add_product, edit_product, delete_product, get_product_details, check_stock
- update_stock, search_products, get_low_stock_alerts, list_products, get_out_of_stock
- get_top_selling_products, update_product_price

### Sales Operations (5 tools)
- record_sale, record_batch_sale, get_sale_history, get_today_sales, record_credit_sale

### Credit Management (3 tools)
- get_customer_balance, get_all_credit_balances, get_overdue_credits

### Customer Management (4 tools)
- add_customer, update_customer, get_customer_details, search_customers

### Payment Operations (3 tools)
- record_payment, get_payment_history, get_today_payments

### Analytics (2 tools)
- get_sales_analytics, get_revenue_summary

### Navigation (2 tools)
- navigate_to_screen, go_home

## Regenerating the Dataset

You can regenerate or customize the dataset using the Python script:

```bash
python3 generate_training_data.py
```

### Customization Options

Edit `generate_training_data.py` to:
- Add more Zambian products
- Add more customer names
- Adjust example counts per category
- Add new command variations
- Change train/eval split ratio

Example modifications:
```python
# Add more products
PRODUCTS["frozen"] = ["Ice Cream", "Frozen Fish", "Frozen Chips"]

# Increase examples for a category
for _ in range(300):  # Changed from 200
    # Generate sale examples...

# Change train/eval split
metadata = "train" if random.random() < 0.8 else "eval"  # 80/20 split
```

## Fine-tuning with Google AI Studio

1. Upload `dukaai_functiongemma_training_data.jsonl` to Google AI Studio
2. Select FunctionGemma model
3. Configure fine-tuning parameters:
   - Learning rate: 0.0001
   - Batch size: 8-16
   - Epochs: 3-5
4. Start fine-tuning job
5. Export trained model

## Model Integration

After fine-tuning, integrate the model into DukaAI:

1. Export the fine-tuned model as TFLite
2. Place in `app/src/main/assets/functiongemma_finetuned.tflite`
3. Update `FunctionGemmaInference.kt` to load the new model
4. Test with voice commands

## Validation

The evaluation examples (955) can be used to validate model performance:

```bash
# Filter evaluation examples
grep '"metadata": "eval"' dukaai_functiongemma_training_data.jsonl > eval_set.jsonl

# Count by category
grep -o '"name": "[^"]*"' eval_set.jsonl | sort | uniq -c
```

## Data Quality

The dataset ensures quality through:
- **Diversity**: Multiple phrasings for each command type
- **Realism**: Authentic Zambian product/customer names
- **Completeness**: All tool parameters covered
- **Balance**: Even distribution across categories
- **Context**: Includes date/time and system prompts

## License

This dataset is part of the DukaAI project and follows the same license.

## Contributing

To improve the dataset:
1. Add more natural language variations
2. Include edge cases and error scenarios
3. Add multi-turn conversations
4. Include Bemba/Nyanja language examples
5. Add voice-specific patterns (stutters, corrections)

## Changelog

### Version 1.0 (2026-01-16)
- Initial release with 2000 examples
- 21 operation categories
- 30 unique tools
- Zambian retail context
