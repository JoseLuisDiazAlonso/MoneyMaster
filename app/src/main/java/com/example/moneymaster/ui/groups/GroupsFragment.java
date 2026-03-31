package com.example.moneymaster.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.databinding.FragmentGroupsBinding;
import com.example.moneymaster.ui.adapter.GroupAdapter;

import java.util.List;

public class GroupsFragment extends Fragment {

    private FragmentGroupsBinding binding;
    private GroupsViewModel       viewModel;
    private GroupAdapter          adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupRecyclerView();
        setupFab();
        observeGroups();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GroupsViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter(group ->
                // Card #34 — navegar al detalle del grupo con tabs
                GroupDetailActivity.start(requireContext(), group.id, group.nombre)
        );

        binding.recyclerViewGroups.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewGroups.setAdapter(adapter);
    }

    private void setupFab() {
        android.util.Log.d("GROUPS_DEBUG", "setupFab llamado");
        binding.fabAddGroup.setOnClickListener(v -> {
            android.util.Log.d("GROUPS_DEBUG", "FAB pulsado");
            Intent intent = new Intent(requireContext(), CreateGroupActivity.class);
            startActivity(intent);
        });
    }

    private void observeGroups() {
        viewModel.getGroupsWithDetails().observe(getViewLifecycleOwner(), groups -> {
            if (groups == null || groups.isEmpty()) {
                showEmptyState();
            } else {
                showGroupList(groups);
            }
            adapter.submitList(groups);
        });
    }

    private void showEmptyState() {
        binding.recyclerViewGroups.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showGroupList(List<?> groups) {
        binding.recyclerViewGroups.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
