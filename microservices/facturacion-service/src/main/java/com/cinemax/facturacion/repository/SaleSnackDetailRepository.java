// repository/SaleSnackDetailRepository.java
public interface SaleSnackDetailRepository extends JpaRepository<SaleSnackDetail, Integer> {
    List<SaleSnackDetail> findBySaleTransaction_IdTransaction(Integer idTransaction);
}