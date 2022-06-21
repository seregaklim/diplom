package com.klim.nework.repository

import com.klim.nework.Api.ApiService
import com.klim.nework.dao.*
import com.klim.nework.db.AppDb
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton




@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModuleAbstract {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}

@InstallIn(SingletonComponent::class)
@Module
 class RepositoryModule {

    @Provides
    @Singleton
    fun providesSignInUpRepository(apiService: ApiService): SignInUpRepository =
        SignInUpRepository(apiService)

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        eventDao: EventDao,
        appDb: AppDb,
        eventRemoteKeyDao: EventRemoteKeyDao
    ): EventRepository = EventRepository(appDb, apiService, eventDao, eventRemoteKeyDao)

    @Provides
    @Singleton
    fun provideProfileRepository(
        apiService: ApiService,
        appDb: AppDb,
        wallRemoteKeyDao: WallRemoteKeyDao,
        wallPostDao: WallPostDao,
        jobDao: JobDao,
        postDao: PostDao,
    ): PageRepository =
        PageRepository(apiService, appDb, wallPostDao, wallRemoteKeyDao, jobDao, postDao)


    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao,
    ): UserRepository =
        UserRepository(apiService, userDao)
}