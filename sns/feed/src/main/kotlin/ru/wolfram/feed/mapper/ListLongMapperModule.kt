package ru.wolfram.feed.mapper

import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper

@Module
interface ListLongMapperModule {
    fun listLongParameterMapper(): R2dbcParameterColumnMapper<List<Long>> {
        return R2dbcParameterColumnMapper { stmt, index, value ->
            value?.let {
                stmt.bind(index, value.toTypedArray())
            }
        }
    }
}