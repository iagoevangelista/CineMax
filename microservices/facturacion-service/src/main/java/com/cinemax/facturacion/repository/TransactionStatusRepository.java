// repository/TransactionStatusRepository.java
public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, Integer> {
    Optional<TransactionStatus> findByNameStatus(String nameStatus);
}