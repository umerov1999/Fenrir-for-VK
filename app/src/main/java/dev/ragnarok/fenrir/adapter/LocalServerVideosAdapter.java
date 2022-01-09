package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.link.VkLinkParser;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.menu.options.VideoLocalServerOption;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.Disposable;

public class LocalServerVideosAdapter extends RecyclerView.Adapter<LocalServerVideosAdapter.Holder> {

    private final Context context;
    private final ILocalServerInteractor mVideoInteractor;
    private List<Video> data;
    private VideoOnClickListener videoOnClickListener;
    private Disposable listDisposable = Disposable.disposed();

    public LocalServerVideosAdapter(@NonNull Context context, @NonNull List<Video> data) {
        this.context = context;
        this.data = data;
        mVideoInteractor = InteractorFactory.createLocalServerInteractor();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_local_server_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Video video = data.get(position);

        holder.title.setText(video.getTitle());
        holder.description.setText(video.getDescription());
        holder.videoLenght.setText(Utils.BytesToSize(video.getDuration()));

        String photoUrl = video.getImage();

        if (Utils.nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.image);
        } else {
            PicassoInstance.with().cancelRequest(holder.image);
        }

        holder.card.setOnClickListener(v -> {
            if (videoOnClickListener != null) {
                videoOnClickListener.onVideoClick(position, video);
            }
        });
        holder.card.setOnLongClickListener(v -> {
            ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
            menus.add(new OptionRequest(VideoLocalServerOption.save_item_video, context.getString(R.string.download), R.drawable.save, true));
            menus.add(new OptionRequest(VideoLocalServerOption.play_item_video, context.getString(R.string.play), R.drawable.play, true));
            menus.add(new OptionRequest(VideoLocalServerOption.update_time_item_video, context.getString(R.string.update_time), R.drawable.ic_recent, false));
            menus.add(new OptionRequest(VideoLocalServerOption.delete_item_video, context.getString(R.string.delete), R.drawable.ic_outline_delete, true));
            menus.add(new OptionRequest(VideoLocalServerOption.edit_item_video, context.getString(R.string.edit), R.drawable.about_writed, true));
            menus.header(firstNonEmptyString(video.getDescription(), " ") + " - " + video.getTitle(), R.drawable.video, null);
            menus.columns(2);
            menus.show(((FragmentActivity) context).getSupportFragmentManager(), "server_video_options", option -> {
                switch (option.getId()) {
                    case VideoLocalServerOption.save_item_video:
                        if (!AppPerms.hasReadWriteStoragePermission(context)) {
                            if (videoOnClickListener != null) {
                                videoOnClickListener.onRequestWritePermissions();
                            }
                            break;
                        }
                        DownloadWorkUtils.doDownloadVideo(context, video, video.getMp4link720(), "Local");
                        break;
                    case VideoLocalServerOption.play_item_video:
                        if (videoOnClickListener != null) {
                            videoOnClickListener.onVideoClick(position, video);
                        }
                        break;
                    case VideoLocalServerOption.update_time_item_video:
                        String hash = VkLinkParser.parseLocalServerURL(video.getMp4link720());
                        if (Utils.isEmpty(hash)) {
                            break;
                        }
                        listDisposable = mVideoInteractor.update_time(hash).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> CustomToast.CreateCustomToast(context).showToast(R.string.success), t -> Utils.showErrorInAdapter((Activity) context, t));
                        break;
                    case VideoLocalServerOption.edit_item_video:
                        String hash2 = VkLinkParser.parseLocalServerURL(video.getMp4link720());
                        if (Utils.isEmpty(hash2)) {
                            break;
                        }
                        listDisposable = mVideoInteractor.get_file_name(hash2).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> {
                            View root = View.inflate(context, R.layout.entry_file_name, null);
                            ((TextInputEditText) root.findViewById(R.id.edit_file_name)).setText(t);
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.change_name)
                                    .setCancelable(true)
                                    .setView(root)
                                    .setPositiveButton(R.string.button_ok, (dialog, which) -> listDisposable = mVideoInteractor.update_file_name(hash2, ((TextInputEditText) root.findViewById(R.id.edit_file_name)).getText().toString().trim())
                                            .compose(RxUtils.applySingleIOToMainSchedulers())
                                            .subscribe(t1 -> CustomToast.CreateCustomToast(context).showToast(R.string.success), o -> Utils.showErrorInAdapter((Activity) context, o)))
                                    .setNegativeButton(R.string.button_cancel, null)
                                    .show();
                        }, t -> Utils.showErrorInAdapter((Activity) context, t));
                        break;
                    case VideoLocalServerOption.delete_item_video:
                        new MaterialAlertDialogBuilder(context)
                                .setMessage(R.string.do_delete)
                                .setTitle(R.string.confirmation)
                                .setCancelable(true)
                                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                                    String hash1 = VkLinkParser.parseLocalServerURL(video.getMp4link720());
                                    if (Utils.isEmpty(hash1)) {
                                        return;
                                    }
                                    listDisposable = mVideoInteractor.delete_media(hash1).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> CustomToast.CreateCustomToast(context).showToast(R.string.success), o -> Utils.showErrorInAdapter((Activity) context, o));
                                })
                                .setNegativeButton(R.string.button_cancel, null)
                                .show();
                        break;
                    default:
                        break;
                }
            });
            return true;
        });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        listDisposable.dispose();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setVideoOnClickListener(VideoOnClickListener videoOnClickListener) {
        this.videoOnClickListener = videoOnClickListener;
    }

    public void setData(List<Video> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface VideoOnClickListener {
        void onVideoClick(int position, Video video);

        void onRequestWritePermissions();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final View card;
        final ImageView image;
        final TextView videoLenght;
        final TextView title;
        final TextView description;

        public Holder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_view);
            image = itemView.findViewById(R.id.video_image);
            videoLenght = itemView.findViewById(R.id.video_lenght);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }
}
