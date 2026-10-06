package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.JdbcUserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@Import({JdbcUserRepository.class, UserRowMapper.class})
@DisplayName("JdbcUserRepository")
public class JdbcUserRepositoryTest {
    public static final long TEST_USER_ID = 1L;
    @Autowired
    JdbcUserRepository userRepository;

    static User getTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail("email@email.com");
        user.setLogin("user");
        user.setName("test");
        user.setBirthday(LocalDate.of(2000, 3, 20));
        return user;
    }

    @Test
    @DisplayName("Пользователь по ид")
    public void shouldReturnUserWhenFindById() {
        User testUser = getTestUser();
        Optional<User> userOptional = userRepository.getUser(testUser.getId());

        assertThat(userOptional)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testUser);
    }

    @Test
    @DisplayName("Пустой логин")
    public void shouldNotCreateUserWithFaillogin() {
        User testUser = getTestUser();
        testUser.setLogin("");
        Optional<User> userOptional = userRepository.create(testUser);

        assertThat(userOptional)
                .isEmpty();
    }

    @Test
    @DisplayName("Некорректный емейл")
    public void shouldNotCreateUserWithFailEmail() {
        User testUser = getTestUser();
        testUser.setEmail("fgfff.ru");
        Optional<User> userOptional = userRepository.create(testUser);

        assertThat(userOptional)
                .isEmpty();
    }

    @Test
    @DisplayName("Некорректная дата рождения")
    public void shouldNotCreateUserWithFailBirthDay() {
        User testUser = getTestUser();
        testUser.setBirthday(LocalDate.of(2111, 01, 11));
        Optional<User> userOptional = userRepository.create(testUser);

        assertThat(userOptional)
                .isEmpty();
    }


}
