package com.simran.orderme.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simran.orderme.Food;
import com.simran.orderme.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private OnImageSelectListener imageSelectListener;

    public FoodAdapter(Context context, List<Food> foodList, OnImageSelectListener imageSelectListener) {
        this.context = context;
        this.foodList = foodList;
        this.imageSelectListener = imageSelectListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDescription, tvPrice;
        ImageView ivFoodImage;
        Button btnEdit, btnDelete;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodNameChef);
            tvDescription = itemView.findViewById(R.id.tvFoodDescriptionChef);
            tvPrice = itemView.findViewById(R.id.tvFoodPriceChef);
            ivFoodImage = itemView.findViewById(R.id.imgFoodChef);
            btnEdit = itemView.findViewById(R.id.btnEditFoodChef);
            btnDelete = itemView.findViewById(R.id.btnDeleteFoodChef);
        }

        public void bind(Food food) {
            tvName.setText(food.getName());
            tvDescription.setText(food.getDescription());
            tvPrice.setText(food.getPrice());

            // Load image with Picasso, with a placeholder and error handling
            if (food.getImage() != null && !food.getImage().isEmpty()) {
                Picasso.get().load(food.getImage()).placeholder(R.drawable.food)
                        .error(R.drawable.ic_launcher_foreground).into(ivFoodImage);
            } else {
                ivFoodImage.setImageResource(R.drawable.food);
            }

            // Edit button functionality
            btnEdit.setOnClickListener(v -> editFood(food));

            // Delete button functionality
            btnDelete.setOnClickListener(v -> deleteFood(food));
        }

        private void deleteFood(Food food) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference foodRef = FirebaseDatabase.getInstance().getReference("foods")
                    .child(currentUserId).child(food.getFid());

            foodRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Food deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete food: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void editFood(Food food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Food");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_food, null);
        builder.setView(dialogView);

        EditText etFoodName = dialogView.findViewById(R.id.etFoodName);
        EditText etFoodDescription = dialogView.findViewById(R.id.etFoodDescription);
        EditText etFoodPrice = dialogView.findViewById(R.id.etFoodPrice);
        ImageView imgFood = dialogView.findViewById(R.id.imgFood);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        // Set existing food details in EditText and ImageView
        etFoodName.setText(food.getName() != null ? food.getName() : "");
        etFoodDescription.setText(food.getDescription() != null ? food.getDescription() : "");
        etFoodPrice.setText(food.getPrice() != null ? food.getPrice() : "");
        if (food.getImage() != null && !food.getImage().isEmpty()) {
            Picasso.get().load(food.getImage()).placeholder(R.drawable.food)
                    .error(R.drawable.ic_launcher_foreground).into(imgFood);
        } else {
            imgFood.setImageResource(R.drawable.food); // Placeholder if no image
        }

        // Image selection button functionality
        btnSelectImage.setOnClickListener(v -> {
            if (imageSelectListener != null) {
                imageSelectListener.onImageSelect(food.getFid());
            }
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedName = etFoodName.getText().toString().trim();
            String updatedDescription = etFoodDescription.getText().toString().trim();
            String updatedPrice = etFoodPrice.getText().toString().trim();

            if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedPrice.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference foodRef = FirebaseDatabase.getInstance().getReference("foods")
                    .child(currentUserId).child(food.getFid());

            // Update food details in Firebase
            foodRef.child("name").setValue(updatedName);
            foodRef.child("description").setValue(updatedDescription);
            foodRef.child("price").setValue(updatedPrice);
            foodRef.child("image").setValue(food.getImage() != null ? food.getImage() : "").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Food updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to update food: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface OnImageSelectListener {
        void onImageSelect(String foodId);
    }
}
