package dev.ragnarok.fenrir.link.internal;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class OwnerLinkSpanFactory {

    private static final Comparator<AbsInternalLink> LINK_COMPARATOR = (link1, link2) -> link1.start - link2.start;
    private static final Pattern ownerPattern;
    private static final Pattern topicCommentPattern;
    private static final Pattern linkPattern;

    static {
        ownerPattern = Pattern.compile("\\[(id|club)(\\d+)\\|([^]]+)]");
        topicCommentPattern = Pattern.compile("\\[(id|club)(\\d*):bp(-\\d*)_(\\d*)\\|([^]]+)]");
        linkPattern = Pattern.compile("\\[(https:[^]]+)\\|([^]]+)]");
    }

    public static Spannable withSpans(String input, boolean owners, boolean topics, ActionListener listener) {
        if (isEmpty(input)) {
            return null;
        }

        List<OwnerLink> ownerLinks = owners ? findOwnersLinks(input) : null;
        List<TopicLink> topicLinks = topics ? findTopicLinks(input) : null;
        List<OtherLink> othersLinks = findOthersLinks(input);

        int count = Utils.safeCountOfMultiple(ownerLinks, topicLinks, othersLinks);

        if (count > 0) {
            List<AbsInternalLink> all = new ArrayList<>(count);

            if (nonEmpty(ownerLinks)) {
                all.addAll(ownerLinks);
            }

            if (nonEmpty(topicLinks)) {
                all.addAll(topicLinks);
            }

            if (nonEmpty(othersLinks)) {
                all.addAll(othersLinks);
            }

            Collections.sort(all, LINK_COMPARATOR);

            Spannable result = Spannable.Factory.getInstance().newSpannable(replace(input, all));
            for (AbsInternalLink link : all) {
                //TODO Нужно ли удалять spannable перед установкой новых
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (listener != null) {
                            if (link instanceof TopicLink) {
                                listener.onTopicLinkClicked((TopicLink) link);
                            }

                            if (link instanceof OwnerLink) {
                                listener.onOwnerClick(((OwnerLink) link).ownerId);
                            }

                            if (link instanceof OtherLink) {
                                listener.onOtherClick(((OtherLink) link).Link);
                            }
                        }
                    }
                };

                result.setSpan(clickableSpan, link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return result;
        }

        return Spannable.Factory.getInstance().newSpannable(input);
    }

    private static int toInt(@Nullable String str, int pow_n) {
        if (isEmpty(str)) {
            return Settings.get().accounts().getCurrent();
        }
        try {
            return Integer.parseInt(str) * pow_n;
        } catch (NumberFormatException ignored) {
        }
        return Settings.get().accounts().getCurrent();
    }

    private static List<TopicLink> findTopicLinks(String input) {
        Matcher matcher = topicCommentPattern.matcher(input);

        List<TopicLink> links = null;
        while (matcher.find()) {
            if (links == null) {
                links = new ArrayList<>(1);
            }

            TopicLink link = new TopicLink();

            boolean club = "club".equals(matcher.group(1));
            link.start = matcher.start();
            link.end = matcher.end();
            link.replyToOwner = toInt(matcher.group(2), (club ? -1 : 1));
            link.topicOwnerId = toInt(matcher.group(3), 1);
            link.replyToCommentId = toInt(matcher.group(4), 1);
            link.targetLine = matcher.group(5);
            links.add(link);
        }

        return links;
    }

    private static List<OwnerLink> findOwnersLinks(String input) {
        List<OwnerLink> links = null;

        Matcher matcher = ownerPattern.matcher(input);
        while (matcher.find()) {
            if (links == null) {
                links = new ArrayList<>(1);
            }

            boolean club = "club".equals(matcher.group(1));
            int ownerId = toInt(matcher.group(2), (club ? -1 : 1));
            String name = matcher.group(3);
            links.add(new OwnerLink(matcher.start(), matcher.end(), ownerId, name));
        }

        return links;
    }

    private static List<OtherLink> findOthersLinks(String input) {
        List<OtherLink> links = null;

        Matcher matcher = linkPattern.matcher(input);
        while (matcher.find()) {
            if (links == null) {
                links = new ArrayList<>(1);
            }
            links.add(new OtherLink(matcher.start(), matcher.end(), matcher.group(1), matcher.group(2)));
        }

        return links;
    }

    public static String getTextWithCollapseOwnerLinks(String input) {
        if (isEmpty(input)) {
            return null;
        }

        List<OwnerLink> links = findOwnersLinks(input);
        return replace(input, links);
    }

    private static String replace(String input, List<? extends AbsInternalLink> links) {
        if (Utils.safeIsEmpty(links)) {
            return input;
        }

        StringBuilder result = new StringBuilder(input);
        for (int y = 0; y < links.size(); y++) {
            AbsInternalLink link = links.get(y);
            int origLenght = link.end - link.start;
            int newLenght = link.targetLine.length();
            shiftLinks(links, link, origLenght - newLenght);
            result.replace(link.start, link.end, link.targetLine);
            link.end = link.end - (origLenght - newLenght);
        }

        return result.toString();
    }

    private static void shiftLinks(List<? extends AbsInternalLink> links, AbsInternalLink after, int count) {
        boolean shiftAllowed = false;
        for (AbsInternalLink link : links) {
            if (shiftAllowed) {
                link.start = link.start - count;
                link.end = link.end - count;
            }

            if (link == after) {
                shiftAllowed = true;
            }
        }
    }

    public static String genOwnerLink(int ownerId, String title) {
        return "[" + (ownerId > 0 ? "id" : "club") + Math.abs(ownerId) + "|" + title + "]";
    }

    public interface ActionListener {
        void onTopicLinkClicked(TopicLink link);

        void onOwnerClick(int ownerId);

        void onOtherClick(String URL);
    }
}
