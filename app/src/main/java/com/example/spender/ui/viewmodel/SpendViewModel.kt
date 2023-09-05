package com.example.spender.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spender.data.DataResult
import com.example.spender.domain.remotemodel.Trip
import com.example.spender.domain.remotemodel.spend.GoogleMapsSpend
import com.example.spender.domain.remotemodel.spend.LocalSpend
import com.example.spender.domain.remotemodel.spend.RemoteSpend
import com.example.spender.domain.remotemodel.spend.Spend
import com.example.spender.domain.repository.SpendRepository
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SpendViewModel @Inject constructor(
    private val repository: dagger.Lazy<SpendRepository>,
    private val appContext: Application
) : ViewModel() {

    /*
     * Create spend
     */

    private val _createSpendDataResult = MutableLiveData<DataResult<String>>()
    val createSpendDataResult: LiveData<DataResult<String>> =
        _createSpendDataResult
    private val _createSpendMsgShow = MutableLiveData<Boolean>()
    val createSpendMsgShow: LiveData<Boolean> = _createSpendMsgShow

    fun createSpend(trip: Trip, spend: LocalSpend) {
        viewModelScope.launch(Dispatchers.IO) {
            _createSpendDataResult.postValue(
                repository.get().createSpend(trip, spend)
            )
        }.invokeOnCompletion {
            _createSpendMsgShow.postValue(true)
        }
    }

    fun doNotShowCreateSpendMsg() {
        _createSpendMsgShow.postValue(false)
    }

    /*
     * Get spends
     */

    private val _getSpendsDataResult = MutableLiveData<DataResult<List<RemoteSpend>>>()
    val getSpendsDataResult: LiveData<DataResult<List<RemoteSpend>>> = _getSpendsDataResult
    private val _getSpendsDataUpdated = MutableLiveData<Boolean>(true)
    val getSpendsDataUpdated: LiveData<Boolean> = _getSpendsDataUpdated

    fun getSpends(trip: Trip) {
        viewModelScope.launch(Dispatchers.IO) {
            _getSpendsDataResult.postValue(
                repository.get().getSpends(trip, Source.CACHE)
            )
        }.invokeOnCompletion {
            _getSpendsDataUpdated.postValue(true)
            if (InternetChecker.check(appContext)) {
                viewModelScope.launch(Dispatchers.IO) {
                    _getSpendsDataResult.postValue(
                        repository.get().getSpends(trip, Source.SERVER)
                    )
                }
            }
        }
    }

    fun getSpendsDataUpdate() {
        _getSpendsDataUpdated.postValue(false)
    }

    /*
     * Update spend
     */

    private val _updateSpendDataResult = MutableLiveData<DataResult<String>>()
    val updateSpendDataResult: LiveData<DataResult<String>> = _updateSpendDataResult
    private val _updateSpendMsgShow = MutableLiveData<Boolean>()
    val updateSpendMsgShow: LiveData<Boolean> = _updateSpendMsgShow

    fun updateSpend(
        oldRemoteSpend: RemoteSpend,
        newRemoteSpend: RemoteSpend
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateSpendDataResult.postValue(
                repository.get().updateSpend(oldRemoteSpend, newRemoteSpend)
            )
        }.invokeOnCompletion {
            _updateSpendMsgShow.postValue(true)
        }
    }

    fun doNotShowUpdateSpendMsg() {
        _updateSpendMsgShow.postValue(false)
    }

    /*
     * Get all spends
     */

    private val _getAllSpendsDataResult = MutableLiveData<DataResult<List<GoogleMapsSpend>>>()
    val getAllSpendsDataResult: LiveData<DataResult<List<GoogleMapsSpend>>> = _getAllSpendsDataResult

    fun getAllSpends() {
        viewModelScope.launch(Dispatchers.IO) {
            _getAllSpendsDataResult.postValue(
                repository.get().getAllSpends(Source.CACHE)
            )
        }.invokeOnCompletion {
            if (InternetChecker.check(appContext)) {
                viewModelScope.launch(Dispatchers.IO) {
                    _getAllSpendsDataResult.postValue(
                        repository.get().getAllSpends(Source.SERVER)
                    )
                }
            }
        }
    }
}
