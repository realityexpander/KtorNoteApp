package com.realityexpander.ktornoteapp.common

import kotlinx.coroutines.flow.*

// NetworkResponseType comes from the network and are used to determine kind of response is being returned
// DatabaseResultType comes from the database and are used to determine kind of result is being returned

inline fun <DatabaseResultType, NetworkResponseType> networkBoundResource(
    crossinline queryDb: () -> Flow<DatabaseResultType>,
    crossinline fetchFromNetwork: suspend () -> NetworkResponseType,
    crossinline saveFetchedResponseToDb: suspend (NetworkResponseType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (DatabaseResultType) -> Boolean = { true },
    crossinline debugNetworkResponseType: (NetworkResponseType) -> Unit = { Unit },
    crossinline debugDatabaseResultType: (DatabaseResultType) -> Unit = { Unit },
) = flow {

    // Helpful to show the extracted types for the inline function
    debugNetworkResponseType(fetchFromNetwork())
    debugDatabaseResultType(queryDb().first())

    // indicate that data is loading
    emit(Resource.loading(null))

    // get current (stale) data from the database
    val staleData = queryDb().first()

    val flow =

        // check network connection
        if (shouldFetch(staleData)) {

            // emit the data we have so far, if any (from the database)
            emit(Resource.loading(null, staleData))

            try {
                // attempt to fetch fresh data from the network
                val response = fetchFromNetwork()

                // save the fresh data to the database
                saveFetchedResponseToDb(response)

                // run the db query to emit the fresh data
                queryDb().map { dbResult ->
                    Resource.success(data = dbResult)
                }
            } catch (t: Throwable) {  // network fetch failed.
                // emit the error from the network fetch
                onFetchFailed(t)

                // emit the stale data from the database anyway, also indicate an error.
                queryDb().map { dbResult ->
                    Resource.error(
                        t.localizedMessage ?: t.message ?: "Couldn't reach server. (is it down?)",
                        data = dbResult
                    )
                }
            }
        } else {
            // return the data we have in the database without fetching from the network
            queryDb().map { dbResult ->
                Resource.success(data = dbResult)
            }
        }

    emitAll(flow)  // shorthand for: flow.collect { value -> emit(value) }
}







































