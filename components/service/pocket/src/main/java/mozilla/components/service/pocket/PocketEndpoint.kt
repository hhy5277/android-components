/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.pocket

import android.support.annotation.WorkerThread
import mozilla.components.concept.fetch.Client
import mozilla.components.service.pocket.data.PocketGlobalVideoRecommendation
import mozilla.components.service.pocket.net.PocketResponse

// See @Ignore tests in test class to do end-to-end testing.

/**
 * Makes requests to the Pocket API and returns the requested data.
 *
 * @see [newInstance] to retrieve an instance.
 */
class PocketEndpoint internal constructor(
    private val rawEndpoint: PocketEndpointRaw,
    private val jsonParser: PocketJSONParser
) {

    /**
     * Gets a response, filled with the Pocket global video recommendations from the Pocket API server on success.
     * This network call is synchronous and the results are not cached. The API version of this call is available in the
     * source code at [PocketURLs.VALUE_VIDEO_RECS_VERSION].
     *
     * If the API returns unexpectedly formatted results, these entries will be omitted and the rest of the items are
     * returned.
     *
     * @return a [PocketResponse.Success] with the Pocket global video recommendations (the list will never be empty)
     * or, on error, a [PocketResponse.Failure].
     */
    @WorkerThread // Synchronous network call.
    fun getGlobalVideoRecommendations(): PocketResponse<List<PocketGlobalVideoRecommendation>> {
        val json = rawEndpoint.getGlobalVideoRecommendations()
        val videoRecs = json?.let { jsonParser.jsonToGlobalVideoRecommendations(it) }
        return videoRecs.wrapInResponse()
    }

    private fun <T> List<T>?.wrapInResponse(): PocketResponse<List<T>> = if (isNullOrEmpty()) {
        PocketResponse.Failure()
    } else {
        PocketResponse.Success(this!!)
    }

    companion object {

        /**
         * Returns a new instance of [PocketEndpoint].
         *
         * @param client the HTTP client to use for network requests.
         * @param pocketApiKey the API key for Pocket network requests.
         * @param userAgent the user agent for network requests.
         *
         * @throws IllegalArgumentException if the provided API key or user agent is deemed invalid.
         */
        fun newInstance(client: Client, pocketApiKey: String, userAgent: String): PocketEndpoint {
            assertIsValidApiKey(pocketApiKey)
            assertIsValidUserAgent(userAgent)

            val endpoint = PocketEndpointRaw(client, PocketURLs(pocketApiKey), userAgent)
            return PocketEndpoint(endpoint, PocketJSONParser())
        }
    }
}

private fun assertIsValidApiKey(apiKey: String) {
    if (apiKey.isBlank()) throw IllegalArgumentException("Expected non-blank API key")
}

private fun assertIsValidUserAgent(userAgent: String) {
    if (userAgent.isBlank()) throw java.lang.IllegalArgumentException("Expected non-blank user agent")
}
