export class ProductMetadataUtil {
  private static readonly COMMON_FIELDS = new Set([
    'id', 'title', 'category', 'productType', 'imageUrl', 'originalValue', 
    'currentPrice', 'currency', 'stockQuantity', 'generalDescription', 
    'status', 'weight', 'length', 'height', 'width', 'barcode', 
    'message', 'result', 'createdByUserId', 'updatedByUserId', 'deletedByUserId'
  ]);

  private static readonly SPEC_LABELS: Record<string, string> = {
    authors: 'AUTHOR',
    publicationDate: 'PUBLICATION DATE',
    pages: 'PAGES',
    nbPages: 'PAGES',
    publisher: 'PUBLISHER',
    coverType: 'COVER',
    genre: 'GENRE',
    language: 'LANGUAGE',
    artists: 'ARTISTS',
    recordLabels: 'RECORD LABELS',
    recordLabel: 'RECORD LABELS',
    releaseDate: 'RELEASE DATE',
    director: 'DIRECTOR',
    discType: 'DISC TYPE',
    runtime: 'RUNTIME',
    studio: 'STUDIO',
    subtitles: 'SUBTITLES',
    editorInChief: 'EDITOR-IN-CHIEF',
    issueNumber: 'ISSUE NUMBER',
    publicationFrequency: 'FREQUENCY',
    issn: 'ISSN',
    sections: 'SECTIONS',
    tracksList: 'TRACKS LIST'
  };

  private static readonly SPEC_SUFFIXES: Record<string, string> = {
    runtime: ' MIN'
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
