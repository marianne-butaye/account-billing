package ca.ulaval.glo4002.accountbilling;

import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountBillingServiceTest {

  private static final BillId BILL_ID = new BillId(18L);

  private AccountBillingService accountBillingService;

  @Mock
  private Bill bill;
  @Mock
  private BillDAO billDao;

  @Before
  public void setUp() {
    accountBillingService = new AccountBillingService();

    BillDAO.setInstance(billDao);
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    BDDMockito.willReturn(true).given(bill).isCancelled();
  }

  @Test(expected = BillNotFoundException.class)
  public void givenUnexistingBillIdWhenCancelInvoiceThenThrowsException() {
    BDDMockito.willReturn(null).given(billDao).findBill(BILL_ID);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);
  }

  @Test
  public void givenBillNotCanceledWhenCancelInvoiceThenVerifyBillIsCanceled() {
    BDDMockito.willReturn(false).given(bill).isCancelled();

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    BDDMockito.verify(bill).cancel();
  }

  @Test
  public void givenBillCanceledWhenCancelInvoiceThenVerifyBillIsNotCanceledAgain() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    BDDMockito.verify(bill, never()).cancel();
  }

}
