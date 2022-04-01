package tests;

import com.example.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    @Test
    void test() {
        String name = "Sava";
        String surname = "Tudor";
        User user = new User("a",name, surname,"oijh");
        user.setId(1);
        assertEquals(user.getId(), 1);
        assertEquals(user.getFirstName(), name);
        assertEquals(user.getLastName(), surname);
        String newName = "Pop";
        String newSurname = "Valentin";
        user.setFirstName(newName);
        user.setLastName(newSurname);

        String anotherName = "David";
        String anotherSurname = "Istrate";
        User otherUser = new User("b",newName, newSurname,"dfg");
        otherUser.setId(1);
        User anotherUser = new User("c",anotherName, anotherSurname,"srtg");
        anotherUser.setId(2);
        assertEquals(user, otherUser);
    }

}
