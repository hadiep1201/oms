export class ProductMetadataUtil {
  private static readonly COMMON_FIELDS = new Set([
    'id', 'title', 'category', 'productType', 'imageUrl', 'originalValue', 
    'currentPrice', 'currency', 'stockQuantity', 'generalDescription', 
    'status', 'weight', 'length', 'height', 'width', 'barcode', 
    'message', 'result', 'createdByUserId', 'updatedByUserId', 'deletedByUserId'
  ]);

  private static readonly SPEC_LABELS: Record<string, string> = {
    authors: 'TÁC GIẢ',
    publicationDate: 'NGÀY XUẤT BẢN',
    pages: 'SỐ TRANG',
    nbPages: 'SỐ TRANG',
    publisher: 'NHÀ XUẤT BẢN',
    coverType: 'LOẠI BÌA',
    genre: 'THỂ LOẠI',
    language: 'NGÔN NGỮ',
    artists: 'NGHỆ SĨ',
    recordLabels: 'HÃNG ĐĨA',
    recordLabel: 'HÃNG ĐĨA',
    releaseDate: 'NGÀY PHÁT HÀNH',
    director: 'ĐẠO DIỄN',
    discType: 'LOẠI ĐĨA',
    runtime: 'THỜI LƯỢNG',
    studio: 'HÃNG PHIM',
    subtitles: 'PHỤ ĐỀ',
    editorInChief: 'TỔNG BIÊN TẬP',
    issueNumber: 'SỐ BÁO',
    publicationFrequency: 'ĐỊNH KỲ',
    issn: 'ISSN',
    sections: 'CHUYÊN MỤC',
    tracksList: 'DANH SÁCH BÀI HÁT'
  };

  private static readonly SPEC_SUFFIXES: Record<string, string> = {
    runtime: ' PHÚT'
  };

  static getDynamicTechnicalDetails(product: any): { label: string, value: any, isArray: boolean, isDate: boolean }[] {
    if (!product) return [];
    const details: { label: string, value: any, isArray: boolean, isDate: boolean }[] = [];
    
    for (const key of Object.keys(product)) {
      if (!this.COMMON_FIELDS.has(key) && product[key] !== null && product[key] !== undefined && product[key] !== '') {
        let value = product[key];
        const isArray = Array.isArray(value);
        let isDate = false;
        
        if (key.toLowerCase().includes('date') && !isArray) {
          isDate = true;
        }
        
        if (!isArray && this.SPEC_SUFFIXES[key]) {
          value = value + this.SPEC_SUFFIXES[key];
        }
        
        details.push({
          label: this.SPEC_LABELS[key] || key.replace(/([A-Z])/g, ' $1').toUpperCase().trim(),
          value: value,
          isArray: isArray,
          isDate: isDate
        });
      }
    }
    return details;
  }

  static isOutOfStock(product: any): boolean {
    return !product || product.stockQuantity <= 0 || product.status?.toUpperCase() === 'DEACTIVATED' || product.status?.toUpperCase() === 'DELETED';
  }
}
