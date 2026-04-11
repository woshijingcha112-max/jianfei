package com.dietrecord.app.core.network

import com.dietrecord.app.core.network.model.ApiEnvelope
import com.dietrecord.app.core.network.model.DateRequest
import com.dietrecord.app.core.network.model.DietRecordSaveRequest
import com.dietrecord.app.core.network.model.FoodSearchRequest
import com.dietrecord.app.core.network.model.GoalSaveRequest
import com.dietrecord.app.core.network.model.IdRequest
import com.dietrecord.app.core.network.model.PeriodRecordRequest
import com.dietrecord.app.core.network.model.RecordIdRequest
import com.dietrecord.app.core.network.model.WeightRecordRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DietApiService {

    @Multipart
    @POST("diet/photo/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): ApiEnvelope<Any>

    @POST("diet/record/save")
    suspend fun saveDietRecord(@Body request: DietRecordSaveRequest): ApiEnvelope<Any>

    @POST("diet/record/list")
    suspend fun listDietRecords(@Body request: DateRequest): ApiEnvelope<Any>

    @POST("diet/record/delete")
    suspend fun deleteDietRecord(@Body request: RecordIdRequest): ApiEnvelope<Any>

    @POST("food/search")
    suspend fun searchFood(@Body request: FoodSearchRequest): ApiEnvelope<Any>

    @POST("food/detail")
    suspend fun foodDetail(@Body request: IdRequest): ApiEnvelope<Any>

    @POST("diet/stat/today")
    suspend fun todayStat(@Body request: DateRequest): ApiEnvelope<Any>

    @POST("goal/get")
    suspend fun getGoal(): ApiEnvelope<Any>

    @POST("goal/save")
    suspend fun saveGoal(@Body request: GoalSaveRequest): ApiEnvelope<Any>

    @POST("weight/record")
    suspend fun saveWeight(@Body request: WeightRecordRequest): ApiEnvelope<Any>

    @POST("period/record")
    suspend fun savePeriod(@Body request: PeriodRecordRequest): ApiEnvelope<Any>

    @POST("period/list")
    suspend fun listPeriods(): ApiEnvelope<Any>
}
