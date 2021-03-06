/*
 * Copyright 2020 Tenkiv, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.tenkiv.coral

@ExperimentalCoralApi
public sealed class Result<out V, out E> {

    /**
     * Returns true if this instance represents successful outcome. In this case [isFailure] returns false.
     */
    @ExperimentalCoralApi
    public abstract val isSuccess: Boolean

    /**
     * Returns true if this instance represents failed outcome. In this case [isSuccess] returns false.
     */
    @ExperimentalCoralApi
    public abstract val isFailure: Boolean

    /**
     * Returns the encapsulated value if this instance represents success or null if it is failure.
     */
    @ExperimentalCoralApi
    public abstract fun getValueOrNull(): V?

    /**
     * Returns the encapsulated error if this instance represents failure or null if it is success.
     */
    @ExperimentalCoralApi
    public abstract fun getErrorOrNull(): E?

    /**
     * Returns the the result of [onSuccess] for encapsulated value if this instance represents success or the result of
     * [onFailure] function for encapsulated error if it is failure.
     */
    @ExperimentalCoralApi
    public inline fun <R> fold(onSuccess: (value: V) -> R, onFailure: (error: E) -> R): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }

    /**
     * Returns the encapsulated result of the given [transform] function applied to encapsulated value
     * if this instance represents [Success] or the
     * original encapsulated exception if it is [Failure].
     */
    @ExperimentalCoralApi
    public inline fun <R> map(transform: (value: V) -> R): Result<R, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Returns the encapsulated result of the given [transform] function applied to encapsulated error
     * if this instance represents [Failure] or the
     * original encapsulated value if it is [Success].
     */
    @ExperimentalCoralApi
    public inline fun <NE> mapError(transform: (error: E) -> NE): Result<V, NE> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }

    /**
     * Performs the given action on encapsulated value if this instance represents [Success].
     * Returns the original [Result] unchanged.
     */
    @ExperimentalCoralApi
    public inline fun onSuccess(action: (value: V) -> Unit): Result<V, E> {
        if (this is Success) action(value)
        return this
    }

    /**
     * Performs the given action on encapsulated error if this instance represents [Failure].
     * Returns the original [Result] unchanged.
     */
    @ExperimentalCoralApi
    public inline fun onFailure(action: (error: E) -> Unit): Result<V, E> {
        if (this is Failure) action(error)
        return this
    }

    @ExperimentalCoralApi
    public data class Success<out V>(public val value: V) : Result<V, Nothing>() {

        /**
         * Always returns true since the [Success] type represents a successful [Result].
         */
        @ExperimentalCoralApi
        public override val isSuccess: Boolean
            get() = true

        /**
         * Always returns false since the [Success] type represents a successful [Result].
         */
        @ExperimentalCoralApi
        public override val isFailure: Boolean
            get() = false

        /**
         * Always returns the encapsulated value since the [Success] type represents a successful [Result].
         */
        @ExperimentalCoralApi
        public override fun getValueOrNull(): V = value

        /**
         * Always returns null since the [Success] type represents a successful [Result].
         */
        @ExperimentalCoralApi
        public override fun getErrorOrNull(): Nothing? = null

    }

    @ExperimentalCoralApi
    public data class Failure<out E>(public val error: E) : Result<Nothing, E>() {

        /**
         * Always returns false since the [Failure] type represents a failed [Result].
         */
        @ExperimentalCoralApi
        public override val isSuccess: Boolean
            get() = false

        /**
         * Always returns true since the [Failure] type represents a failed [Result].
         */
        @ExperimentalCoralApi
        public override val isFailure: Boolean
            get() = true

        /**
         * Always returns null since the [Failure] type represents a failed [Result].
         */
        @ExperimentalCoralApi
        public override fun getValueOrNull(): Nothing? = null

        /**
         * Always returns the encapsulated error since the [Failure] type represents a failed [Result].
         */
        @ExperimentalCoralApi
        public override fun getErrorOrNull(): E = error

    }

}

@ExperimentalCoralApi
public inline fun <V, E, R> Result<V, E>.flatMap(transform: (value: V) -> Result<R, E>): Result<R, E> = when (this) {
    is Result.Success -> transform(value)
    is Result.Failure -> this
}

@ExperimentalCoralApi
public inline fun <V, E, NE> Result<V, E>.flatMapError(transform: (error: E) -> Result<V, NE>): Result<V, NE> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> transform(error)
    }

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or the
 * result of [onFailure] function for encapsulated error if it is [Result.Failure].
 */
@ExperimentalCoralApi
public inline fun <E, V> Result<V, E>.getOrElse(onFailure: (error: E) -> V): V = when (this) {
    is Result.Success -> this.value
    is Result.Failure -> onFailure(error)
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or the
 * [defaultValue] if it is [Result.Failure].
 *
 * This function is shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
 */
@ExperimentalCoralApi
public fun <E, V> Result<V, E>.getOrDefault(defaultValue: V): V = getOrElse { defaultValue }

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or throws the encapsulated exception
 * if it is [Result.Failure].
 */
@ExperimentalCoralApi
public fun <V> Result<V, Throwable>.getOrThrow(): V = getOrElse { throw it }

/**
 * Throws the encapsulated exception.
 */
@ExperimentalCoralApi
public fun Result.Failure<Throwable>.throwException(): Nothing = throw error

@ExperimentalCoralApi
public fun <V> kotlin.Result<V>.toCoralResult(): Result<V, Throwable> =
    fold(onSuccess = {
        Result.Success(it)
    },
        onFailure = {
            Result.Failure(it)
        })
