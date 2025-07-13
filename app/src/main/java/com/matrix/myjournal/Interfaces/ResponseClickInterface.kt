package com.matrix.myjournal.Interfaces

interface ResponseClickInterface {
    fun deleteResponse(position: Int)
    fun deleteImage(position: Int)
    fun updateResponse(position: Int, id: Int)
    fun editResponse(position: Int, entryId: Int)
}
