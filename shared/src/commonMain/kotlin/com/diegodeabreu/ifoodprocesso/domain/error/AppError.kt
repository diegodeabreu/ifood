package com.diegodeabreu.ifoodprocesso.domain.error

sealed interface AppError {
    data class Network(val message: String?) : AppError
    data class Database(val message: String?) : AppError
    data class Unknown(val message: String?) : AppError
}

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Error(val error: AppError) : DomainResult<Nothing>
}
