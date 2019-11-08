package com.company.p9.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.company.p9.api.Api;
import com.company.p9.api.ApiModule;
import com.company.p9.db.AppDatabase;
import com.company.p9.db.AppDao;
import com.company.p9.model.ApiResponse;
import com.company.p9.model.Item;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    public enum Sort { DATE, ABC }

    private AppDao dao;
    private Api api;

    private MutableLiveData<Boolean> requery = new MutableLiveData<>();

    private String searchTerm = "";
    private Sort sort = Sort.DATE;

    private MutableLiveData<String> termLiveData = new MutableLiveData<>();
    private MutableLiveData<Sort> sortLiveData = new MutableLiveData<>();

    public LiveData<List<Item>> apiItemList = Transformations.switchMap(requery, new Function<Boolean, LiveData<List<Item>>>() {
        @Override
        public LiveData<List<Item>> apply(final Boolean input) {
            final MutableLiveData<List<Item>> items = new MutableLiveData<>();

            api.buscar().enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    List<Item> itemList = new ArrayList<>();
                    for(Item item:response.body().items){
                        if(item.name.contains(searchTerm)){
                            itemList.add(item);
                        }
                    }
                    items.setValue(itemList);
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("ABC", "API NETWORK FAILURE");
                }
            });

            return items;
        }
    });

    public LiveData<List<Item>> dbItemList = Transformations.switchMap(requery, new Function<Boolean, LiveData<List<Item>>>() {
        @Override
        public LiveData<List<Item>> apply(Boolean input) {
            if(sort == Sort.DATE) {
                return dao.getItemsByDate("%" + searchTerm + "%");
            }
            return dao.getItemsByName("%" + searchTerm + "%");
        }
    });

    public MainViewModel(@NonNull Application application) {
        super(application);

        dao = AppDatabase.getInstance(application).itemDao();
        api = ApiModule.api;
    }

    public void setSearchTerm(String newTerm){
        searchTerm = newTerm;
        requery.setValue(true);
//        termLiveData.setValue(newTerm);
    }

    public void setSort(Sort newSort) {
        Log.e("ABCD", "SET SORT to " + newSort);
        sort = newSort;
        requery.setValue(true);

//        sortLiveData.setValue(sort);
    }
}
