package xyz.yakdmt.prefetcherapp.models

object HeavyModelFactory {

    fun create(howMany: Int) : List<HeavyModel> {
        val result = mutableListOf<HeavyModel>()
        for (i in 0..howMany) {
            result.add(HeavyModel(i.toString()))
        }
        return result
    }
}