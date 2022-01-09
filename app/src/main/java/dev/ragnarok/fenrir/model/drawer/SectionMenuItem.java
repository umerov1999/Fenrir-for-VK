package dev.ragnarok.fenrir.model.drawer;

import android.os.Parcel;

public class SectionMenuItem extends AbsMenuItem {

    public static Creator<SectionMenuItem> CREATOR = new Creator<SectionMenuItem>() {
        public SectionMenuItem createFromParcel(Parcel source) {
            return new SectionMenuItem(source);
        }

        public SectionMenuItem[] newArray(int size) {
            return new SectionMenuItem[size];
        }
    };
    private int section;
    private int title;
    private int count;

    public SectionMenuItem(int type, int section, int title) {
        super(type);
        this.section = section;
        this.title = title;
    }

    public SectionMenuItem(Parcel in) {
        super(in);
        section = in.readInt();
        title = in.readInt();
        count = in.readInt();
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + section;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(section);
        dest.writeInt(title);
        dest.writeInt(count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SectionMenuItem that = (SectionMenuItem) o;
        return section == that.section;
    }
}
