package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.findById;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.GroupSettingsDto;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiBanned;
import dev.ragnarok.fenrir.db.interfaces.IOwnersStorage;
import dev.ragnarok.fenrir.db.model.BanAction;
import dev.ragnarok.fenrir.domain.IGroupSettingsInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.ContactInfo;
import dev.ragnarok.fenrir.model.Day;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class GroupSettingsInteractor implements IGroupSettingsInteractor {

    private final INetworker networker;

    private final IOwnersStorage repository;

    private final IOwnersRepository ownersRepository;

    public GroupSettingsInteractor(INetworker networker, IOwnersStorage repository, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.repository = repository;
        this.ownersRepository = ownersRepository;
    }

    private static ContactInfo transform(VKApiCommunity.Contact contact) {
        return new ContactInfo(contact.user_id)
                .setDescriprion(contact.desc)
                .setEmail(contact.email)
                .setPhone(contact.phone);
    }

    private static Day parseDateCreated(String text) {
        if (isEmpty(text)) {
            return null;
        }

        String[] parts = text.split("\\.");

        return new Day(
                parseInt(parts, 0, 0),
                parseInt(parts, 1, 0),
                parseInt(parts, 2, 0)
        );
    }

    private static int parseInt(String[] parts, int index, int ifNotExists) {
        if (parts.length <= index) {
            return ifNotExists;
        }

        return Integer.parseInt(parts[index]);
    }

    @Override
    public Single<GroupSettings> getGroupSettings(int accountId, int groupId) {
        return networker.vkDefault(accountId)
                .groups()
                .getSettings(groupId)
                .flatMap(dto -> Single.just(createFromDto(dto)));
    }

    @Override
    public Completable ban(int accountId, int groupId, int ownerId, Long endDateUnixtime, int reason, String comment, boolean commentVisible) {
        return networker.vkDefault(accountId)
                .groups()
                .ban(groupId, ownerId, endDateUnixtime, reason, comment, commentVisible)
                .andThen(repository.fireBanAction(new BanAction(groupId, ownerId, true)));
    }

    @Override
    public Completable editManager(int accountId, int groupId, User user, String role, boolean asContact, String position, String email, String phone) {
        String targetRole = "creator".equalsIgnoreCase(role) ? "administrator" : role;

        return networker.vkDefault(accountId)
                .groups()
                .editManager(groupId, user.getId(), targetRole, asContact, position, email, phone)
                .andThen(Single
                        .fromCallable(() -> {
                            ContactInfo info = new ContactInfo(user.getId())
                                    .setDescriprion(position)
                                    .setPhone(phone)
                                    .setEmail(email);

                            return new Manager(user, role)
                                    .setContactInfo(info)
                                    .setDisplayAsContact(asContact);
                        })
                        .flatMapCompletable(manager -> repository.fireManagementChangeAction(Pair.Companion.create(groupId, manager))));
    }

    @Override
    public Completable unban(int accountId, int groupId, int ownerId) {
        return networker.vkDefault(accountId)
                .groups()
                .unban(groupId, ownerId)
                .andThen(repository.fireBanAction(new BanAction(groupId, ownerId, false)));
    }

    @Override
    public Single<Pair<List<Banned>, IntNextFrom>> getBanned(int accountId, int groupId, IntNextFrom startFrom, int count) {
        IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + count);

        return networker.vkDefault(accountId)
                .groups()
                .getBanned(groupId, startFrom.getOffset(), count, Constants.MAIN_OWNER_FIELDS, null)
                .map(Items::getItems)
                .flatMap(items -> {
                    VKOwnIds ids = new VKOwnIds();

                    for (VkApiBanned u : items) {
                        ids.append(u.banInfo.adminId);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(bundle -> {
                                List<Banned> infos = new ArrayList<>(items.size());

                                for (VkApiBanned u : items) {
                                    User admin;
                                    VkApiBanned.Info banInfo = u.banInfo;

                                    if (banInfo.adminId != 0) {
                                        admin = (User) bundle.getById(u.banInfo.adminId);
                                    } else {
                                        // ignore this
                                        continue;
                                    }

                                    Banned.Info info = new Banned.Info()
                                            .setComment(banInfo.comment)
                                            .setCommentVisible(banInfo.commentVisible)
                                            .setDate(banInfo.date)
                                            .setEndDate(banInfo.endDate)
                                            .setReason(banInfo.reason);

                                    if (u.profile != null) {
                                        infos.add(new Banned(Dto2Model.transformUser(u.profile), admin, info));
                                    } else if (u.group != null) {
                                        infos.add(new Banned(Dto2Model.transformCommunity(u.group), admin, info));
                                    }
                                }

                                return Pair.Companion.create(infos, nextFrom);
                            });
                });
    }

    @Override
    public Single<List<ContactInfo>> getContacts(int accountId, int groupId) {
        return networker.vkDefault(accountId).groups()
                .getById(Collections.singleton(groupId), null, null, "contacts")
                .map(communities -> {
                    List<VKApiCommunity.Contact> temps = listEmptyIfNull(communities.get(0).contacts);
                    List<ContactInfo> managers = new ArrayList<>(temps.size());
                    for (VKApiCommunity.Contact user : temps) {
                        managers.add(transform(user));
                    }
                    return managers;
                });
    }

    @Override
    public Single<List<Manager>> getManagers(int accountId, int groupId) {
        return networker.vkDefault(accountId)
                .groups()
                .getMembers(String.valueOf(groupId), null, null, null, Constants.MAIN_OWNER_FIELDS, "managers")
                .flatMap(items -> networker.vkDefault(accountId)
                        .groups()
                        .getById(Collections.singleton(groupId), null, null, "contacts")
                        .map(communities -> {
                            if (communities.isEmpty()) {
                                throw new NotFoundException("Group with id " + groupId + " not found");
                            }
                            return listEmptyIfNull(communities.get(0).contacts);
                        })
                        .map(contacts -> {
                            List<VKApiUser> users = listEmptyIfNull(items.getItems());
                            List<Manager> managers = new ArrayList<>(users.size());
                            for (VKApiUser user : users) {
                                VKApiCommunity.Contact contact = findById(contacts, user.id);
                                Manager manager = new Manager(Dto2Model.transformUser(user), user.role);
                                if (nonNull(contact)) {
                                    manager.setDisplayAsContact(true).setContactInfo(transform(contact));
                                }
                                managers.add(manager);
                            }
                            return managers;
                        }));
    }

    private IdOption createFromDto(GroupSettingsDto.PublicCategory category) {
        return new IdOption(category.id, category.name, createFromDtos(category.subtypes_list));
    }

    private List<IdOption> createFromDtos(List<GroupSettingsDto.PublicCategory> dtos) {
        if (isEmpty(dtos)) {
            return Collections.emptyList();
        }

        List<IdOption> categories = new ArrayList<>(dtos.size());
        for (GroupSettingsDto.PublicCategory dto : dtos) {
            categories.add(createFromDto(dto));
        }

        return categories;
    }

    private GroupSettings createFromDto(GroupSettingsDto dto) {
        List<IdOption> categories = createFromDtos(dto.public_category_list);

        IdOption category = null;
        IdOption subcategory = null;

        if (nonNull(dto.public_category)) {
            category = findById(categories, Integer.parseInt(dto.public_category));

            if (nonNull(dto.public_subcategory) && nonNull(category)) {
                subcategory = findById(category.getChilds(), Integer.parseInt(dto.public_subcategory));
            }
        }

        return new GroupSettings()
                .setTitle(dto.title)
                .setDescription(dto.description)
                .setAddress(dto.address)
                .setAvailableCategories(categories)
                .setCategory(category)
                .setSubcategory(subcategory)
                .setWebsite(dto.website)
                .setDateCreated(parseDateCreated(dto.public_date))
                .setFeedbackCommentsEnabled(dto.wall == 1)
                .setObsceneFilterEnabled(dto.obscene_filter)
                .setObsceneStopwordsEnabled(dto.obscene_stopwords)
                .setObsceneWords(Utils.join(dto.obscene_words, ",", orig -> orig));
    }
}