// repository/SaleTicketDetailRepository.java
public interface SaleTicketDetailRepository extends JpaRepository<SaleTicketDetail, Integer> {
    List<SaleTicketDetail> findBySaleTransaction_IdTransaction(Integer idTransaction);
    boolean existsByIdShowtimeAndIdSeat(Integer idShowtime, Integer idSeat);
}