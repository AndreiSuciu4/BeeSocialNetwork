package tests;

import com.example.domain.Status;
import com.example.domain.User;
import com.example.domain.UsersFriendsDTO;
import com.example.domain.UsersRequestsDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.example.build.Build.formatter;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DtoTests {
    @Test
    void testUsersFriendsDTO() {
        User user1 = new User("sava", "tudor"), user2 = new User("suciu", "andrei");
        UsersFriendsDTO dto = new UsersFriendsDTO(user1,user2, LocalDateTime.of(2021,11,23,11,30));
        assertEquals(dto.getUsera(), user1);
        assertEquals(dto.getUserb(), user2);
        assertEquals(dto.getDate().format(formatter),"2021-11-23 11:30:00" );
        assertEquals(dto.toString(), "andrei | suciu | 2021-11-23 11:30:00");
    }

    @Test
    void testUsersRequestsDTO(){
        User user1 = new User("sava", "tudor"), user2 = new User("suciu", "andrei");
        user1.setId(1);
        user2.setId(2);
        UsersRequestsDTO dto = new UsersRequestsDTO(user1,user2, Status.APPROVED);
        assertEquals(dto.getFrom(), user1);
        assertEquals(dto.getTo(), user2);
        assertEquals(dto.getStatus(), Status.APPROVED);
        assertEquals(dto.toString(), "from=1 | sava tudor, to=2 | suciu andrei, status=APPROVED");
    }
}
