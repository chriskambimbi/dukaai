package com.example.dukaai.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.dukaai.ml.BarcodeScanner
import com.example.dukaai.ml.DefaultBarcodeScanner
import com.example.dukaai.ml.ProductClassifier
import com.example.dukaai.ml.TFLiteProductClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension property for DataStore
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "duka_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    /**
     * Provides DataStore for settings persistence
     */
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provides ProductClassifier for ML-based product recognition
     */
    @Provides
    @Singleton
    fun provideProductClassifier(
        @ApplicationContext context: Context
    ): ProductClassifier {
        val classifier = TFLiteProductClassifier(context)
        // Initialize the classifier
        try {
            classifier.initialize()
        } catch (e: Exception) {
            // Log error but continue - app can work without ML
            android.util.Log.e("AppModule", "Failed to initialize ML classifier", e)
        }
        return classifier
    }

    /**
     * Provides BarcodeScanner
     */
    @Provides
    @Singleton
    fun provideBarcodeScanner(): BarcodeScanner {
        return DefaultBarcodeScanner()
    }

    /**
     * Provides Firebase Firestore
     */
    @Provides
    @Singleton
    fun provideFirestore(): com.google.firebase.firestore.FirebaseFirestore {
        return com.google.firebase.firestore.FirebaseFirestore.getInstance()
    }

    /**
     * Provides Firebase Auth
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }
}
