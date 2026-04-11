package com.dietrecord.app.core.network

import com.dietrecord.app.core.network.model.ApiEnvelope
import com.dietrecord.app.core.network.model.DietDateDTO
import com.dietrecord.app.core.network.model.DietRecordCardVO
import com.dietrecord.app.core.network.model.DietRecordSaveDTO
import com.dietrecord.app.core.network.model.GoalGetVO
import com.dietrecord.app.core.network.model.GoalSaveDTO
import com.dietrecord.app.core.network.model.PhotoUploadVO
import com.dietrecord.app.core.network.model.TodayDietStatVO
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DietApiService {

    @POST("goal/get")
    suspend fun getGoal(): ApiEnvelope<GoalGetVO>

    @POST("goal/save")
    suspend fun saveGoal(@Body request: GoalSaveDTO): ApiEnvelope<Void>

    @Multipart
    @POST("diet/photo/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): ApiEnvelope<PhotoUploadVO>

    @POST("diet/record/save")
    suspend fun saveDietRecord(@Body request: DietRecordSaveDTO): ApiEnvelope<Void>

    @POST("diet/record/list")
    suspend fun listDietRecords(@Body request: DietDateDTO): ApiEnvelope<List<DietRecordCardVO>>

    @POST("diet/stat/today")
    suspend fun todayStat(@Body request: DietDateDTO): ApiEnvelope<TodayDietStatVO>
}
