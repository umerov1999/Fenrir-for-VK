package dev.ragnarok.fenrir.model;


public class MessageUpdate {

    private final int accountId;

    private final int messageId;

    private StatusUpdate statusUpdate;

    private ImportantUpdate importantUpdate;

    private DeleteUpdate deleteUpdate;

    public MessageUpdate(int accountId, int messageId) {
        this.accountId = accountId;
        this.messageId = messageId;
    }

    public DeleteUpdate getDeleteUpdate() {
        return deleteUpdate;
    }

    public void setDeleteUpdate(DeleteUpdate deleteUpdate) {
        this.deleteUpdate = deleteUpdate;
    }

    public ImportantUpdate getImportantUpdate() {
        return importantUpdate;
    }

    public void setImportantUpdate(ImportantUpdate importantUpdate) {
        this.importantUpdate = importantUpdate;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getMessageId() {
        return messageId;
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }

    public void setStatusUpdate(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public static class ImportantUpdate {
        private final boolean important;

        public ImportantUpdate(boolean important) {
            this.important = important;
        }

        public boolean isImportant() {
            return important;
        }
    }

    public static class DeleteUpdate {

        private final boolean deleted;
        private final boolean deletedForAll;

        public DeleteUpdate(boolean deleted, boolean deletedForAll) {
            this.deleted = deleted;
            this.deletedForAll = deletedForAll;
        }

        public boolean isDeletedForAll() {
            return deletedForAll;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }

    public static class StatusUpdate {

        @MessageStatus
        private final int status;

        private final Integer vkid;

        public StatusUpdate(@MessageStatus int status, Integer vkid) {
            this.status = status;
            this.vkid = vkid;
        }

        public Integer getVkid() {
            return vkid;
        }

        @MessageStatus
        public int getStatus() {
            return status;
        }
    }
}