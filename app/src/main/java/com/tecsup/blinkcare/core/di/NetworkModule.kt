package com.tecsup.blinkcare.core.di


import dagger.Module
import dagger.hilt.InstallIn
import com.tecsup.blinkcare.core.network.ApiService
import com.tecsup.blinkcare.core.utils.IpManager
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideIpManager(): IpManager {
        return IpManager()
    }

    @Provides
    @Singleton
    fun provideRetrofit(ipManager: IpManager): Retrofit {
        val baseUrl = ipManager.getBaseUrl()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
