package com.example.appoficina

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Serviço simples para buscar nome/descrição via Open Food Facts (sem chave)
object ExternalProductService {

    private const val BASE_URL = "https://world.openfoodfacts.org/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api: OpenFoodFactsApi by lazy { retrofit.create(OpenFoodFactsApi::class.java) }

    fun buscarProduto(
        barcode: String,
        onSuccess: (nome: String?, descricao: String?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        api.getProduct(barcode).enqueue(object : Callback<OpenFoodFactsResponse> {
            override fun onResponse(
                call: Call<OpenFoodFactsResponse>,
                response: Response<OpenFoodFactsResponse>
            ) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == 1 && body.product != null) {
                    val p = body.product
                    val nome = p.product_name
                        ?: p.generic_name
                        ?: p.brands
                    val descricao = p.generic_name
                        ?: listOfNotNull(p.brands, p.categories).joinToString(" | ")
                        .takeIf { it.isNotBlank() }
                    onSuccess(nome, descricao)
                } else {
                    onSuccess(null, null)
                }
            }

            override fun onFailure(call: Call<OpenFoodFactsResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    fun getProduct(@Path("barcode") barcode: String): Call<OpenFoodFactsResponse>
}

data class OpenFoodFactsResponse(
    val status: Int = 0,
    val product: Product? = null
)

data class Product(
    val product_name: String? = null,
    val generic_name: String? = null,
    val brands: String? = null,
    val categories: String? = null
)