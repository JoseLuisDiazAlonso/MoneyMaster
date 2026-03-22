package com.example.moneymaster.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moneymaster.data.model.GroupWithDetails;
import com.example.moneymaster.data.repository.GrupoRepository;

import java.util.List;

public class GroupsViewModel extends AndroidViewModel {

    private final GrupoRepository grupoRepository;
    private final LiveData<List<GroupWithDetails>> groupsWithDetails;

    public GroupsViewModel(@NonNull Application application) {
        super(application);
        grupoRepository = new GrupoRepository(application);
        groupsWithDetails = grupoRepository.getAllGroupsWithDetails();
    }

    public LiveData<List<GroupWithDetails>> getGroupsWithDetails() {
        return groupsWithDetails;
    }
}
