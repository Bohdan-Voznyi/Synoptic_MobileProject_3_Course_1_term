// src/test/kotlin/com/example/sunoptic/WeatherTests.kt

package com.example.sunoptic

import com.example.sunoptic.network.*
import com.google.gson.Gson
import junit.framework.Assert.assertNotNull
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherModelTest {

    private val gson = Gson()

    @Test
    fun forecastResponseDeserialization() {
        val json = """
            {
                "city": {"name":"Kyiv","coord":{"lat":50.45,"lon":30.52}},
                "list":[{"dt":169,"main":{"temp":10.0,"humidity":80,"pressure":1012},
                        "weather":[{"description":"drizzle","icon":"09d"}],
                        "visibility":10000,
                        "dt_txt":"2025-11-16 12:00:00",
                        "wind":{"speed":3.5,"deg":180}
                }]
            }
        """
        val response = gson.fromJson(json, ForecastResponse::class.java)
        assertEquals("Kyiv", response.city.name)
        assertEquals(10.0, response.list[0].main.temp)
        assertEquals("drizzle", response.list[0].weather[0].description)
    }

    @Test
    fun airPollutionResponseDeserialization() {
        val json = """
            {
                "list":[
                    {"main":{"aqi":3},
                     "components":{"co":200.0,"no2":5.5,"o3":70.1,"so2":2.1,"pm2_5":10.5,"pm10":15.2}}
                ]
            }
        """
        val response = gson.fromJson(json, AirPollutionResponse::class.java)
        assertEquals(3, response.list[0].aqiData.aqi)
        assertEquals(200.0, response.list[0].components.co)
        assertEquals(15.2, response.list[0].components.pm10)
    }

    @Test
    fun aqiValueToTextConversion() {
        fun aqiToText(aqi: Int): String = when(aqi) {
            1 -> "Добре"
            2 -> "Задовільно"
            3 -> "Помірно"
            4 -> "Погано"
            5 -> "Дуже погано"
            else -> "Невідомо"
        }

        assertEquals("Добре", aqiToText(1))
        assertEquals("Помірно", aqiToText(3))
        assertEquals("Дуже погано", aqiToText(5))
        assertEquals("Невідомо", aqiToText(0))
    }
}

class WeatherApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WeatherApiService

    @Before
    fun setup() {
        server = MockWebServer()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun getForecastReturnsCorrectCity() = runBlocking {
        val mockResponse = """{"city":{"name":"Kyiv","coord":{"lat":50.45,"lon":30.52}},"list":[]}"""
        server.enqueue(MockResponse().setBody(mockResponse))
        val response = api.getForecast("Kyiv", "API_KEY")
        assertEquals("Kyiv", response.city.name)
    }

    @Test
    fun getForecastHandlesEmptyList() = runBlocking {
        val mockResponse = """{"city":{"name":"Lviv","coord":{"lat":49.84,"lon":24.03}},"list":[]}"""
        server.enqueue(MockResponse().setBody(mockResponse))
        val response = api.getForecast("Lviv", "API_KEY")
        assertNotNull(response.list)
        assertEquals(0, response.list.size)
    }

    @Test
    fun getAirPollutionReturnsCorrectAqi() = runBlocking {
        val mockResponse = """
            {
                "list":[{"main":{"aqi":2},"components":{"co":200.0,"no2":5.5,"o3":70.1,"so2":2.1,"pm2_5":10.5,"pm10":15.2}}]
            }
        """
        server.enqueue(MockResponse().setBody(mockResponse))
        val response = api.getAirPollution(50.45, 30.52, "API_KEY")
        assertEquals(2, response.list[0].aqiData.aqi)
        assertEquals(200.0, response.list[0].components.co)
    }

    @Test
    fun getAirPollutionHandlesMultipleItems() = runBlocking {
        val mockResponse = """
            {
                "list":[
                    {"main":{"aqi":1},"components":{"co":100.0,"no2":2.0,"o3":50.0,"so2":1.0,"pm2_5":5.0,"pm10":10.0}},
                    {"main":{"aqi":3},"components":{"co":150.0,"no2":3.0,"o3":60.0,"so2":1.5,"pm2_5":7.0,"pm10":12.0}}
                ]
            }
        """
        server.enqueue(MockResponse().setBody(mockResponse))
        val response = api.getAirPollution(50.45, 30.52, "API_KEY")
        assertEquals(2, response.list.size)
        assertEquals(3, response.list[1].aqiData.aqi)
        assertEquals(12.0, response.list[1].components.pm10)
    }

}
