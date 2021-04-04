package com.tomi.ohl.szakdoga.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tomi.ohl.szakdoga.MainActivity;
import com.tomi.ohl.szakdoga.R;
import com.tomi.ohl.szakdoga.adapters.ShoppingListRecyclerViewAdapter;
import com.tomi.ohl.szakdoga.controller.StorageController;
import com.tomi.ohl.szakdoga.models.ShoppingListItem;
import com.tomi.ohl.szakdoga.views.AddShoppingListItemBottomSheet;
import com.tomi.ohl.szakdoga.views.TopFadingEdgeRecyclerView;

import java.util.LinkedHashMap;

public class ShoppingListFragment extends Fragment {
    private LinkedHashMap<String, ShoppingListItem> shoppingListMap;
    private TopFadingEdgeRecyclerView rv;

    public ShoppingListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // A fragment layoutja
        View layout = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        // Javaslatok megjelenítése
        ChipGroup chipGroup = layout.findViewById(R.id.shoppinglistSuggestionsGroup);
        loadSuggestions(chipGroup);

        // Új üzenet gomb
        FloatingActionButton newMessageFab = layout.findViewById(R.id.fabAddShopping);
        newMessageFab.setOnClickListener(view -> {
            AddShoppingListItemBottomSheet addItemSheet = new AddShoppingListItemBottomSheet();
            addItemSheet.show(getChildFragmentManager(), AddShoppingListItemBottomSheet.class.getSimpleName());
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadShoppingList();
    }

    // 5 javaslat betöltése, többi törlése
    private void loadSuggestions(ChipGroup chipGroup) {
        StorageController.getInstance().getSuggestionItems().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.size() > 0) {
                    int count = 0;
                    for (DocumentSnapshot elem : querySnapshot) {
                        count++;
                        if (count < 6) {
                            String itemName = (String) elem.get("name");
                            Chip suggestion = (Chip) getLayoutInflater().inflate(R.layout.chip_suggestion, chipGroup, false);
                            suggestion.setText(itemName);
                            suggestion.setCheckable(false);
                            suggestion.setOnClickListener(view -> onSuggestionClicked(elem.getId(), chipGroup, suggestion, itemName));
                            suggestion.setOnCloseIconClickListener(view -> onCloseSuggestion(elem.getId(), chipGroup, suggestion));
                            chipGroup.addView(suggestion);
                        } else {
                            StorageController.getInstance().deleteSuggestionItem(elem.getId());
                        }
                    }
                }
            }
        });
    }

    private void onSuggestionClicked(String itemId, ChipGroup chipGroup, Chip chip, String itemName) {
        StorageController.getInstance().insertShoppingListItem(new ShoppingListItem(itemName, false));
        onCloseSuggestion(itemId, chipGroup, chip);
    }

    private void onCloseSuggestion(String itemId, ChipGroup chipGroup, Chip chip) {
        chipGroup.removeView(chip);
        StorageController.getInstance().deleteSuggestionItem(itemId);
    }

    // Bevásárlólista betöltése
    private void loadShoppingList() {
        shoppingListMap = new LinkedHashMap<>();
        rv = requireView().findViewById(R.id.shoppinglistRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireView().getContext()));
        rv.setAdapter(new ShoppingListRecyclerViewAdapter(shoppingListMap));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback());
        itemTouchHelper.attachToRecyclerView(rv);
        // Bevásárlólista lekérése
        ((MainActivity)requireActivity()).getDbListeners().add(StorageController.getInstance().getShoppingListItems().addSnapshotListener(
                (value, error) -> {
                    assert value != null;
                    shoppingListMap.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId();
                        ShoppingListItem item = doc.toObject(ShoppingListItem.class);
                        shoppingListMap.put(id, item);
                    }
                    if (rv.getAdapter() != null)
                        ((ShoppingListRecyclerViewAdapter)rv.getAdapter()).updateKeys(shoppingListMap.keySet());
                }
        ));
    }

    // RecyclerView-n való csúsztatás kezelése
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // A csúsztatott elem törlése
                int position = viewHolder.getAdapterPosition();
                ShoppingListRecyclerViewAdapter adapter = (ShoppingListRecyclerViewAdapter) rv.getAdapter();
                if (adapter != null) {
                    String key = adapter.getKeyAtPosition(position);
                    adapter.setLastDeletedItemPosition(position);
                    StorageController.getInstance().deleteShoppingListItem(key);
                }
            }
        };
    }
}