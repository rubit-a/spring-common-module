package rubit.coreexcel.download

interface ExcelDownloadService {
    fun download(request: ExcelDownloadRequest): ExcelDownloadResult
}
