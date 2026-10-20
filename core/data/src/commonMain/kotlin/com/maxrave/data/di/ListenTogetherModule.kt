package com.maxrave.data.di

import com.maxrave.data.listentogether.ListenTogetherRepositoryImpl
import com.maxrave.domain.repository.ListenTogetherRepository
import org.koin.dsl.module

/** The XYZ build has no Listen Together client; the repository is a no-op that never connects. */
val listenTogetherModule =
    module {
        single<ListenTogetherRepository> { ListenTogetherRepositoryImpl() }
    }
