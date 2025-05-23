package com.example.artsyfrontend.data.model

/**
 * 对应后端 /artist/search/{query} 返回
 */
data class SearchResponse(
    val artists: List<SearchArtist>
)

data class SearchArtist(
    val id: String,
    val name: String,
    val image: String?
)
