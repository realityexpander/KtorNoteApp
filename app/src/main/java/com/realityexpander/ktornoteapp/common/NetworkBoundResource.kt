package com.realityexpander.ktornoteapp.common

import kotlinx.coroutines.flow.*

// NwResponseType comes from the network and are used to determine what kind of request is being made
// DbResultType comes from the database and are used to determine what kind of result is being returned

inline fun <DbResultType, NwResponseType> networkBoundResource(
    crossinline queryDb: () -> Flow<DbResultType>,                          // data from the database
    crossinline fetchFromNetwork: suspend () -> NwResponseType,             // data from the network
    crossinline saveFetchResponseToDb: suspend (NwResponseType) -> Unit,    // save to the database
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (DbResultType) -> Boolean = { true },
    crossinline debugNwResponseType: (NwResponseType) -> Unit = { Unit },
    crossinline debugDbResultType: (DbResultType) -> Unit = { Unit },
) = flow {

    debugNwResponseType(fetchFromNetwork())
    debugDbResultType(queryDb().first())

    // indicate that data is loading
    emit(Resource.loading(null))

    // get stale data from the database
    val staleData = queryDb().first()

    val flow =
        // check network connection
        if (shouldFetch(staleData)) { // data is ignored
            // emit the data we have so far, if any (from the database)
            emit(Resource.loading(null, staleData))

            try {
                // attempt to fetch fresh data from the network
                val response = fetchFromNetwork()

                // save the fresh data to the database
                saveFetchResponseToDb(response)

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







































