package com.bitec.saafs.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bitec.saafs.R;
import com.bitec.saafs.interfaces.IPropertyChangeListener;
import com.bitec.saafs.models.Comment;
import com.bitec.saafs.ui.activities.friends.FriendProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class StreamsCommentsAdapter extends BaseRecyclerViewAdapter<Comment> {

    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private boolean isOwner;
    
    private List<IPropertyChangeListener> propertyChangeListeners = new ArrayList<> ();

    public StreamsCommentsAdapter (Context context, boolean owner) {
        this.context = context;
        this.isOwner=owner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        return new ViewHolder(view);
    }
    
	@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
	
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolder holder = (ViewHolder)viewHolder;
       if(isOwner){
           enableDeletion(holder);
       }else{

           if (getItem (position).getId().equals(mCurrentUser.getUid())){
              enableDeletion(holder);
           }

       }

        holder.username.setText(getItem(position).getUsername());
       holder.username.setOnClickListener ( v -> FriendProfile.startActivity ( context, getItem( holder.getAdapterPosition () ).getId () ) );

       holder.image.setOnClickListener ( v -> FriendProfile.startActivity ( context, getItem( holder.getAdapterPosition () ).getId () ) );

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_6))
                .load(getItem(position).getImage())
                .into(holder.image);

        holder.comment.setText(getItem(position).getComment());

        String timeAgo = TimeAgo.using(Long.parseLong(getItem(position).getTimestamp()));
        holder.timestamp.setText(String.format(Locale.ENGLISH,"Commented %s", timeAgo));

        try {
            mFirestore.collection("Users")
                    .document(getItem(position).getId())
                    .get()
                    .addOnSuccessListener ( documentSnapshot -> {

                        try {
                            if (!documentSnapshot.getString("username").equals(getItem(holder.getAdapterPosition()).getUsername()) &&
                                    !documentSnapshot.getString("image").equals(getItem(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("username", documentSnapshot.getString("username"));
                                commentMap.put("image", documentSnapshot.getString("image"));

                                mFirestore.collection("Streams")
                                        .document(getItem(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(getItem(holder.getAdapterPosition()).getCommentId())
                                        .update(commentMap)
                                        .addOnSuccessListener ( aVoid -> Log.i ( "comment_update", "success" ) )
                                        .addOnFailureListener ( e -> Log.i ( "comment_update", "failure" ) );

                                holder.username.setText(documentSnapshot.getString("username"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            } else if (!documentSnapshot.getString("username").equals(getItem(holder.getAdapterPosition()).getUsername())) {


                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("username", documentSnapshot.getString("username"));

                                mFirestore.collection("Streams")
                                        .document(getItem(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(getItem(holder.getAdapterPosition()).getCommentId())
                                        .update(commentMap)
                                        .addOnSuccessListener ( aVoid -> Log.i ( "comment_update", "success" ) )
                                        .addOnFailureListener ( e -> Log.i ( "comment_update", "failure" ) );

                                holder.username.setText(documentSnapshot.getString("username"));

                            } else if (!documentSnapshot.getString("image").equals(getItem(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> commentMap = new HashMap<>();
                                commentMap.put("image", documentSnapshot.getString("image"));

                                mFirestore.collection("Streams")
                                        .document(getItem(holder.getAdapterPosition()).getPost_id())
                                        .collection("Comments")
                                        .document(getItem(holder.getAdapterPosition()).getCommentId())
                                        .update(commentMap)
                                        .addOnSuccessListener ( aVoid -> Log.i ( "comment_update", "success" ) )
                                        .addOnFailureListener ( e -> Log.i ( "comment_update", "failure" ) );

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);

                            }


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    } )
                    .addOnFailureListener ( e -> Log.e ( "Error", e.getMessage () ) );
        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }
        
        for(ViewHolderBinding binding : mViewHolderBinding)
        {
            binding.BindViewHolder ( holder,getItem (position),position );
        }
    }
    
    public void addPropertyChangedListener(IPropertyChangeListener listener)
    {
        propertyChangeListeners.add ( listener );
    }

    private void enableDeletion(final ViewHolder holder) {

        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setAlpha(0.0f);

        holder.delete.animate()
                .alpha(1.0f)
                .start();

        holder.delete.setOnClickListener ( v -> new MaterialDialog.Builder( context)
                .title("Delete comment")
                .content("Are you sure you want to delete your comment?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive ( (dialog, which) -> {
                    dialog.dismiss();

                    final ProgressDialog progressDialog=new ProgressDialog(context);
                    progressDialog.setMessage("Deleting comment...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    mFirestore.collection("Streams")
                            .document(mItemsList.get(holder.getAdapterPosition()).getPost_id())
                            .collection("Comments")
                            .document(mItemsList.get(holder.getAdapterPosition()).getCommentId ())
                            .delete()
                            .addOnSuccessListener ( aVoid -> {
                                mItemsList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyDataSetChanged();
                                onPropertyChanged(CommentsPropety.removed.name(),holder.getAdapterPosition());
                                progressDialog.dismiss();
                                Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                            } )
                            .addOnFailureListener ( e -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Error deleting comment: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                Log.w("Error","delete comment",e);
                            } );

                } )
                .onNegative ( (dialog, which) -> dialog.dismiss () )
                .show() );

    }
    
    @Override
    public void onPropertyChanged (String propertyName, Object property) {
        
        for(IPropertyChangeListener listener : propertyChangeListeners)
            listener.onPropertyChanged ( propertyName,property );
    }

    public enum CommentsPropety
    {removed,added}
    
    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView username, comment, timestamp;
        private ImageView delete;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            image = mView.findViewById(R.id.comment_user_image);
            username = mView.findViewById(R.id.comment_username);
            comment = mView.findViewById(R.id.comment_text);
            timestamp = mView.findViewById(R.id.comment_timestamp);
            delete=mView.findViewById(R.id.delete);

        }
    }
    
    
}
