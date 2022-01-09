package dev.ragnarok.fenrir.api.model.longpoll;

public class WriteTextInDialogUpdate extends AbsLongpollEvent {

    public int peer_id;
    public int[] from_ids;
    public int from_ids_count;
    public boolean is_text;

    public WriteTextInDialogUpdate(boolean is_text) {
        super(is_text ? ACTION_USER_WRITE_TEXT_IN_DIALOG : ACTION_USER_WRITE_VOICE_IN_DIALOG);
        this.is_text = is_text;
    }
}