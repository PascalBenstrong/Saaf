package com.bitec.saafs.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bitec.saafs.R;
import com.bitec.saafs.models.Message;
import com.bitec.saafs.models.MessageReply;
import com.bitec.saafs.ui.activities.notification.NotificationImage;
import com.bitec.saafs.ui.activities.notification.NotificationImageReply;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageImageReplyAdapter extends RecyclerView.Adapter<MessageImageReplyAdapter.ViewHolder> {

    private List<MessageReply> messageList;
    private Context context;
    private FirebaseFirestore mFirestore;

    public MessageImageReplyAdapter(List<MessageReply> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageImageReplyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_text_item,parent,false);
        mFirestore=FirebaseFirestore.getInstance();
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
    public void onBindViewHolder(@NonNull final MessageImageReplyAdapter.ViewHolder holder, int position) {

        try {
            if (messageList.get(position).isRead()) {
                holder.read_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.read_icon));
                holder.read_icon.setVisibility(View.VISIBLE);
                holder.read_icon.setAlpha(0.0f);
                holder.read_icon.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();
            } else {
                holder.read_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.unread_icon));
                holder.read_icon.setVisibility(View.VISIBLE);
                holder.read_icon.setAlpha(0.0f);
                holder.read_icon.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mFirestore.collection("Users")
                .document(messageList.get(position).getFrom())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Glide.with(context)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                .load(documentSnapshot.getString("image"))
                                .into(holder.image);

                        holder.name.setText(documentSnapshot.getString("name"));

                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, NotificationImageReply.class);
                intent.putExtra("doc_id", messageList.get(holder.getAdapterPosition()).msgId);
                intent.putExtra("read", messageList.get(holder.getAdapterPosition()).isRead());
                intent.putExtra("from_id", messageList.get(holder.getAdapterPosition()).getFrom());
                intent.putExtra("message", messageList.get(holder.getAdapterPosition()).getMessage());
                intent.putExtra("reply_for", messageList.get(holder.getAdapterPosition()).getReply_for());
                intent.putExtra("image", messageList.get(holder.getAdapterPosition()).getReply_image());
                context.startActivity(intent);

                messageList.get(holder.getAdapterPosition()).setRead(true);
                holder.read_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.read_icon));
                holder.read_icon.setVisibility(View.VISIBLE);
                holder.read_icon.setAlpha(0.0f);
                holder.read_icon.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();
            }
        });

        String timeAgo = TimeAgo.using(Long.parseLong(messageList.get(holder.getAdapterPosition()).getTimestamp()));
        holder.time.setText(timeAgo);

        holder.message.setText(String.format(Locale.ENGLISH,"Reply for image you sent with message: %s\nMessage: %s",messageList.get(position).getReply_for(),messageList.get(position).getMessage()));

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialDialog.Builder(context)
                        .title("Delete message")
                        .content("Are you sure do you want to delete this message?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                mFirestore.collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Notifications_reply_image")
                                        .document(messageList.get(holder.getAdapterPosition()).msgId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                messageList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                                notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace();
                                            }
                                        });

                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView message,name,time;
        private ImageView read_icon;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            name = mView.findViewById(R.id.name);
            message = mView.findViewById(R.id.message);
            time = mView.findViewById(R.id.time);
            read_icon=mView.findViewById(R.id.read);

        }
    }
}
