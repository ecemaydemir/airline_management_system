package airlinesystem.concurrent;

/**
 * Rapor hazır olduğunda GUI'ye (veya konsola) haber vermek için kullanılan callback arayüzü.
 * GUI tarafında bu arayüzü implemente edip, sonucu orada ekrana basacaksın.
 */
public interface ReportListener {

    /**
     * Rapor hazırlanmayı bitirdiğinde çağrılır.
     * @param reportText Tüm uçuşlar için doluluk oranlarını içeren metin raporu.
     */
    void onReportReady(String reportText);
}
