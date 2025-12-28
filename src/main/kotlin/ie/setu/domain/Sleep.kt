package ie.setu.domain

data class Sleep(
    val id: Int,
    val userId: Int,
    val duration: Double,
    //rating 1 -10
    val quality: Int,
    val date: String
)
