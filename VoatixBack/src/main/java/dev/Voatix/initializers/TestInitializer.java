package dev.Voatix.initializers;

import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
//todo: lombok инициализация, переделать на дто репозиторий,
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TestInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final IdeaRepository ideaRepository;
    private final PasswordEncoder encoder;
    private final ModeratorRepository moderatorRepository;
    private final VotingEstimatesRepository votingEstimatesRepository;
    private final CommentRepository commentRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.count() > 0) {
            return;
        }

        // Пользователи
        UserEntity admin = createUser("admin", "admin", RoleOfUserEnum.ADMIN);
        UserEntity user1 = createUser("manager1", "manager1", RoleOfUserEnum.USER);
        UserEntity user2 = createUser("manager2", "manager2", RoleOfUserEnum.USER);

        // Проекты
        ProjectEntity coffeeWay = projectRepository.save(
                ProjectEntity.builder()
                        .title("CoffeeWay")
                        .active(true)
                        .avatar(null)
                        .build()
        );

        ProjectEntity liarsBar = projectRepository.save(
                ProjectEntity.builder()
                        .title("Liar`s bar")
                        .active(true)
                        .avatar(null)
                        .build()
        );

        ProjectEntity pryanichki = projectRepository.save(
                ProjectEntity.builder()
                        .title("Прянички от дяди Вовы")
                        .active(true)
                        .avatar(null)
                        .build()
        );

        // Роли в проектах
        addModerator(user1, coffeeWay, RoleOfProjectManager.OWNER);
        addModerator(user2, coffeeWay, RoleOfProjectManager.MANAGER);

        addModerator(user1, liarsBar, RoleOfProjectManager.MANAGER);
        addModerator(user1, pryanichki, RoleOfProjectManager.MANAGER);

        // Идеи — только для CoffeeWay
        createIdea(coffeeWay, user1,
                "Бесплатный Wi-Fi",
                "Давать клиентам за покупку пароль от вайфая кофейни",
                "01.05.25", IdeaStatusEnum.DONE);

        createIdea(coffeeWay, user2,
                "Установить более удобные стулья",
                "Текущие стулья выглядят красиво, но сидеть на них долго неудобно",
                "12.08.25", IdeaStatusEnum.IN_WORK);
        createIdea(coffeeWay, user1,
                "Уменьшить порции кофе",
                "Не хочется переплачивать за кофе, которое я всё равно не выпью",
                "16.06.25", IdeaStatusEnum.CANCELLED);
        createIdea(coffeeWay, user2,
                "Добавить сезонные напитки",
                "Хотелось бы больше лимонадов и авторских напитков летом",
                "03.07.25", IdeaStatusEnum.CREATED);
        createIdea(coffeeWay, admin,
                "Ввести бонусную систему",
                "Копить баллы за покупки и обменивать на напитки",
                "22.04.25", IdeaStatusEnum.IN_WORK);
        createIdea(coffeeWay, user1,
                "Установить зарядки для телефонов",
                "Очень не хватает USB‑розеток возле столиков",
                "11.03.25", IdeaStatusEnum.DONE);
        createIdea(coffeeWay, user2,
                "Добавить растительное молоко бесплатно",
                "Сейчас за него приходится доплачивать, что не очень приятно",
                "19.09.25", IdeaStatusEnum.CREATED);
        createIdea(coffeeWay, admin,
                "Сделать музыку тише",
                "Иногда слишком громко, сложно работать или разговаривать",
                "28.10.25", IdeaStatusEnum.IN_WORK);

        createIdea(coffeeWay, user1,
                "Добавить больше десертов",
                "Ассортимент вкусный, но хотелось бы больше выбора",
                "05.11.25", IdeaStatusEnum.CREATED);

        // Дополнительные пользователи (без владения проектами)
        List<UserEntity> extraUsers = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            UserEntity u = createUser("user_extra_" + i, "pass" + i, RoleOfUserEnum.USER);
            extraUsers.add(u);
        }

        // Дополнительные идеи + лайки/дизлайки
        for (UserEntity u : extraUsers) {

            IdeaEntity idea1 = createIdeaAndReturn(
                    coffeeWay,
                    u,
                    "Идея от " + u.getNickname(),
                    "Описание идеи от " + u.getNickname(),
                    "10.01.25",
                    IdeaStatusEnum.CREATED
            );
            generateRandomVotes(idea1, extraUsers);

            IdeaEntity idea2 = createIdeaAndReturn(
                    coffeeWay,
                    u,
                    "Вторая идея от " + u.getNickname(),
                    "Второе описание идеи от " + u.getNickname(),
                    "15.02.25",
                    IdeaStatusEnum.IN_WORK
            );
            generateRandomVotes(idea2, extraUsers);
        }
    }

    // ---------------- Методы ----------------

    private void addComment(IdeaEntity idea, UserEntity author, String text) {
        CommentEntity c = new CommentEntity();
        c.setIdea(idea);
        c.setUser(author);
        c.setText(text);
        c.setDateTime(new Timestamp(System.currentTimeMillis()));
        commentRepository.save(c);
    }


    private UserEntity createUser(String nickname, String rawPassword, RoleOfUserEnum role) {

        UserEntity user = new UserEntity();
        user.setNickname(nickname);

        user = userRepository.save(user);

        PasswordEntity password = new PasswordEntity();
        password.setPassword(encoder.encode(rawPassword));
        password.setDateOfChange(new Timestamp(System.currentTimeMillis()));

        CredentialsEntity credentials = new CredentialsEntity();
        credentials.setId(user.getId());
        credentials.setUser(user);
        credentials.setActive(true);
        credentials.setRole(role);
        credentials.setPassword(password);

        user.setCredentials(credentials);

        return userRepository.save(user);
    }

    private void createIdea(ProjectEntity project,
                            UserEntity author,
                            String title,
                            String description,
                            String date,
                            IdeaStatusEnum status) {

        IdeaEntity idea = new IdeaEntity();
        idea.setTitle(title);
        idea.setUser(author);
        idea.setDescription(description);
        idea.setStatus(status);
        idea.setProject(project);

        String formatted = "2025-" + date.substring(3, 5) + "-" + date.substring(0, 2) + " 00:00:00";
        idea.setDateTime(Timestamp.valueOf(formatted));

        ideaRepository.save(idea);

        UserEntity user = userRepository.findById(2L).get();

        VotingEstimatesEntity v = new VotingEstimatesEntity();
        v.setIdea(idea);
        v.setUser(user);
        v.setIsLike(false);
        votingEstimatesRepository.save(v);
        //Комментиарии
        addComment(idea, user, "Отличная идея!");
        addComment(idea, user, "Поддерживаю");
        addComment(idea, user, "Не уверен, что это нужно");
        addComment(idea, user, "шрусшзтщлтзлмтлщ");
        addComment(idea, user, "Тестовый комментарий");

    }

    private ModeratorEntity addModerator(UserEntity user, ProjectEntity project, RoleOfProjectManager role) {
        ModeratorEntity m = new ModeratorEntity();
        m.setUser(user);
        m.setProject(project);
        m.setRole(role);
        return moderatorRepository.save(m);
    }

    private IdeaEntity createIdeaAndReturn(ProjectEntity project,
                                           UserEntity author,
                                           String title,
                                           String description,
                                           String date,
                                           IdeaStatusEnum status) {

        IdeaEntity idea = new IdeaEntity();
        idea.setTitle(title);
        idea.setUser(author);
        idea.setDescription(description);
        idea.setStatus(status);
        idea.setProject(project);

        String formatted = "2025-" + date.substring(3, 5) + "-" + date.substring(0, 2) + " 00:00:00";
        idea.setDateTime(Timestamp.valueOf(formatted));

        return ideaRepository.save(idea);
    }

    private void generateRandomVotes(IdeaEntity idea, List<UserEntity> users) {

        int totalUsers = users.size();

        int likesCount = random.nextInt(totalUsers + 1);
        int dislikesCount = random.nextInt(totalUsers - likesCount + 1);

        List<UserEntity> shuffled = new ArrayList<>(users);
        Collections.shuffle(shuffled);

        List<UserEntity> likeUsers = shuffled.subList(0, likesCount);
        List<UserEntity> dislikeUsers = shuffled.subList(likesCount, likesCount + dislikesCount);

        for (UserEntity u : likeUsers) {
            VotingEstimatesEntity v = new VotingEstimatesEntity();
            v.setIdea(idea);
            v.setUser(u);
            v.setIsLike(true);
            votingEstimatesRepository.save(v);
        }

        for (UserEntity u : dislikeUsers) {
            VotingEstimatesEntity v = new VotingEstimatesEntity();
            v.setIdea(idea);
            v.setUser(u);
            v.setIsLike(false);
            votingEstimatesRepository.save(v);
        }
    }
}
