package com.example.marik.maporganizer.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;

import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.db.TaskItem;
import com.example.marik.maporganizer.adapters.viewHolder.TaskHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

public class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

    public List<TaskItem> mItems = new ArrayList<>();
    ;
    Context mContext;
    OnItemsListClicked mListClickedListener;
    android.support.v7.view.ActionMode mActionMode;
    TreeSet<Integer> mCheckedItems = new TreeSet<>();
    public boolean isSelected = false;


    public TaskAdapter(Context context) {
        mContext = context;
    }

    public void setList(List<TaskItem> list) {
        mItems.clear();
        mItems.addAll(list);
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                final int type = viewType;
                final ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams sglp =
                            (StaggeredGridLayoutManager.LayoutParams) lp;
                    sglp.setFullSpan(false);
                    sglp.width = view.getWidth();
                    view.setLayoutParams(sglp);
                    final StaggeredGridLayoutManager lm =
                            (StaggeredGridLayoutManager) ((RecyclerView) parent).getLayoutManager();
                    lm.invalidateSpanAssignments();
                }
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskHolder holder, final int position) {
        holder.bindHolder(mItems.get(position));

        if (mActionMode != null) {
            if (isSelected) {
                holder.getDeleteCheckBox().setVisibility(View.VISIBLE);
                holder.getDeleteCheckBox().setChecked(mCheckedItems.contains(holder.getAdapterPosition()));

            }
        } else {
            holder.getDeleteCheckBox().setChecked(false);
            holder.getDeleteCheckBox().setVisibility(View.INVISIBLE);
        }

        holder.getDeleteCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectItem(holder.getAdapterPosition(), true);


                } else
                    selectItem(holder.getAdapterPosition(), false);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionMode == null && mListClickedListener != null) {
                    mListClickedListener.onClickItem(mItems.get(holder.getAdapterPosition()));
                } else
                    holder.getDeleteCheckBox().setChecked(!holder.getDeleteCheckBox().isChecked());
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mActionMode == null) {
                    mActionMode = ((AppCompatActivity) holder.itemView.getContext()).startSupportActionMode(new android.support.v7.view.ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                            mode.getMenuInflater().inflate(R.menu.menu_task_list, menu);
                            holder.getDeleteCheckBox().setVisibility(View.VISIBLE);

                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(final android.support.v7.view.ActionMode mode, MenuItem item) {
                            if (mCheckedItems.size() != 0) {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                                builder.setMessage("Delete " + mCheckedItems.size() + " items?")
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (mListClickedListener != null) {
                                                    for (Integer i : mCheckedItems) {
                                                        mListClickedListener.onRemove((mItems.get(i)).getId());
                                                    }
                                                }
                                                mode.finish();
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .create().show();
                            } else {
                                mode.finish();
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
                            mActionMode = null;
                            mCheckedItems.clear();
                        }
                    });

                    selectItem(holder.getAdapterPosition(), true);
                    notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    public void setListClickedListener(OnItemsListClicked listClickedListener) {
        mListClickedListener = listClickedListener;
    }

    void selectItem(int position, boolean isSelect) {
        if (isSelect) {
            mCheckedItems.add(position);
            isSelected = true;
        } else {
            mCheckedItems.remove(position);
            isSelected = false;

        }

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public TaskItem getTaskAtPosition(int position) {
        return mItems.get(position);
    }

    public void addItem(TaskItem item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }


    public interface OnItemsListClicked {
        void onClickItem(TaskItem item);

        void onRemove(UUID id);

    }
}


