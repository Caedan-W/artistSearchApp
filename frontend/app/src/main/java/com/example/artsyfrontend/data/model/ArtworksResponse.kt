// ArtworksResponse.kt — 对应 /artist/:id/artworks
package com.example.artsyfrontend.data.model

data class ArtworksResponse(
    val artworks: List<Artwork>
)

data class Artwork(
    val id: String,
    val title: String?,
    val date: String?,
    val image: String?
)