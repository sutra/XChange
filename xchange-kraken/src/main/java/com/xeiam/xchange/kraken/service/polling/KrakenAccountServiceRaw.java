package com.xeiam.xchange.kraken.service.polling;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.Currency;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.kraken.dto.account.KrakenDepositAddress;
import com.xeiam.xchange.kraken.dto.account.KrakenDepositMethods;
import com.xeiam.xchange.kraken.dto.account.KrakenLedger;
import com.xeiam.xchange.kraken.dto.account.KrakenTradeBalanceInfo;
import com.xeiam.xchange.kraken.dto.account.KrakenTradeVolume;
import com.xeiam.xchange.kraken.dto.account.LedgerType;
import com.xeiam.xchange.kraken.dto.account.Withdraw;
import com.xeiam.xchange.kraken.dto.account.WithdrawInfo;
import com.xeiam.xchange.kraken.dto.account.results.KrakenBalanceResult;
import com.xeiam.xchange.kraken.dto.account.results.KrakenDepositAddressResult;
import com.xeiam.xchange.kraken.dto.account.results.KrakenDepositMethodsResults;
import com.xeiam.xchange.kraken.dto.account.results.KrakenLedgerResult;
import com.xeiam.xchange.kraken.dto.account.results.KrakenQueryLedgerResult;
import com.xeiam.xchange.kraken.dto.account.results.KrakenTradeBalanceInfoResult;
import com.xeiam.xchange.kraken.dto.account.results.KrakenTradeVolumeResult;
import com.xeiam.xchange.kraken.dto.account.results.WithdrawInfoResult;
import com.xeiam.xchange.kraken.dto.account.results.WithdrawResult;

/**
 * @author jamespedwards42
 */
public class KrakenAccountServiceRaw extends KrakenBasePollingService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public KrakenAccountServiceRaw(Exchange exchange) {

    super(exchange);
  }

  /**
   * To avoid having to map to Kraken currency codes (e.g., ZUSD) use {@link KrakenAccountService#getAccountInfo} instead.
   *
   * @return Map of Kraken Assets to account balance
   * @throws IOException
   */
  public Map<String, BigDecimal> getKrakenBalance() throws IOException {

    KrakenBalanceResult balanceResult = kraken.balance(exchange.getExchangeSpecification().getApiKey(), signatureCreator, exchange.getNonceFactory());
    return checkResult(balanceResult);
  }

  public KrakenDepositAddress[] getDepositAddresses(String currency, String method, boolean newAddress) throws IOException {
    KrakenDepositAddressResult depositAddressesResult = kraken.getDepositAddresses(null, currency, method,
        exchange.getExchangeSpecification().getApiKey(), signatureCreator, exchange.getNonceFactory());
    return checkResult(depositAddressesResult);
  }

  public KrakenDepositMethods[] getDepositMethods(String assetPairs, String assets) throws IOException {
    KrakenDepositMethodsResults depositMethods = kraken.getDepositMethods(assetPairs, assets, exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());
    return checkResult(depositMethods);
  }

  public WithdrawInfo getWithdrawInfo(String assetPairs, String assets, String key, BigDecimal amount) throws IOException {
    WithdrawInfoResult withdrawInfoResult = kraken.getWithdrawInfo(assetPairs, assets, key, amount, exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());
    return checkResult(withdrawInfoResult);
  }

  public Withdraw withdraw(String assetPairs, String assets, String key, BigDecimal amount) throws IOException {
    WithdrawResult withdrawResult = kraken.withdraw(assetPairs, assets, key, amount, exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());
    return checkResult(withdrawResult);
  }

  /**
   * @param valuationCurrency - Base asset used to determine balance (can be null, defaults to USD). The asset should be provided in the form of a
   *        standard currency code, i.e., EUR. It will be converted to the appropriate Kraken Asset code.
   * @return KrakenTradeBalanceInfo
   * @throws IOException
   */
  public KrakenTradeBalanceInfo getKrakenTradeBalance(Currency valuationCurrency) throws IOException {

    String valuationCurrencyCode = null;

    if (valuationCurrency != null) {
      valuationCurrencyCode = getKrakenCurrencyCode(valuationCurrency);
    }

    KrakenTradeBalanceInfoResult balanceResult = kraken.tradeBalance(null, valuationCurrencyCode, exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());
    return checkResult(balanceResult);
  }

  /**
   * Retrieves the user's trade balance using the default currency ZUSD to determine the balance.
   *
   * @return KrakenTradeBalanceInfo
   * @throws IOException
   */
  public KrakenTradeBalanceInfo getKrakenTradeBalance() throws IOException {

    return getKrakenTradeBalance(null);
  }

  /**
   * Retrieves the full account Ledger which represents all account asset activity.
   *
   * @return
   * @throws IOException
   */
  public Map<String, KrakenLedger> getKrakenLedgerInfo() throws IOException {

    return getKrakenLedgerInfo(null, null, null, null);
  }

  /**
   * Retrieves the Ledger which represents all account asset activity.
   *
   * @param assets - Set of assets to restrict output to (can be null, defaults to all)
   * @param ledgerType - {@link LedgerType} to retrieve (can be null, defaults to all types)
   * @param start - starting unix timestamp or ledger id of results (can be null)
   * @param end - ending unix timestamp or ledger id of results (can be null)
   * @param offset - result offset (can be null)
   * @return
   * @throws IOException
   */
  public Map<String, KrakenLedger> getKrakenLedgerInfo(LedgerType ledgerType, String start, String end, String offset, Currency... assets)
      throws IOException {

    String ledgerTypeString = (ledgerType == null) ? "all" : ledgerType.toString().toLowerCase();

    KrakenLedgerResult ledgerResult = kraken.ledgers(null, delimitAssets(assets), ledgerTypeString, start, end, offset,
        exchange.getExchangeSpecification().getApiKey(), signatureCreator, exchange.getNonceFactory());
    return checkResult(ledgerResult).getLedgerMap();
  }

  public Map<String, KrakenLedger> queryKrakenLedger(String... ledgerIds) throws IOException {

    KrakenQueryLedgerResult ledgerResult = kraken.queryLedgers(createDelimitedString(ledgerIds), exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());

    return checkResult(ledgerResult);
  }

  public KrakenTradeVolume getTradeVolume(CurrencyPair... currencyPairs) throws IOException {
    KrakenTradeVolumeResult result = kraken.tradeVolume(delimitAssetPairs(currencyPairs), exchange.getExchangeSpecification().getApiKey(),
        signatureCreator, exchange.getNonceFactory());
    return checkResult(result);
  }
}
