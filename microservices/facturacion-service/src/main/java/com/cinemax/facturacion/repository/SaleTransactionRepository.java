// repository/SaleTransactionRepository.java
public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Integer> {
    List<SaleTransaction> findByIdUserOrderByCreatedAtDesc(Integer idUser);
}