package com.matrix.myjournal.DataClasses

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var title: String? = null
    var questionsListSize:Int = 0
    val username = MutableLiveData<String>() // LiveData for name
    val email = MutableLiveData<String>()
}
