package hei.school.exam.datastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ListGrouperTest {

  private final ListGrouper<Integer> listGrouper = new ListGrouper<>();

  @Test
  void groups_list_into_even_chunks() {
    List<List<Integer>> result = listGrouper.apply(List.of(1, 2, 3, 4), 2);

    assertThat(result).containsExactly(List.of(1, 2), List.of(3, 4));
  }

  @Test
  void groups_list_with_remainder_in_last_chunk() {
    List<List<Integer>> result = listGrouper.apply(List.of(1, 2, 3, 4, 5), 2);

    assertThat(result).containsExactly(List.of(1, 2), List.of(3, 4), List.of(5));
  }

  @Test
  void returns_empty_list_for_empty_input() {
    assertThat(listGrouper.apply(List.of(), 3)).isEmpty();
  }

  @Test
  void single_group_when_groupSize_exceeds_list_size() {
    List<List<Integer>> result = listGrouper.apply(List.of(1, 2), 10);

    assertThat(result).containsExactly(List.of(1, 2));
  }
}
