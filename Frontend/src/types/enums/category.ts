export enum Category {
    COMPUTER_SCIENCE = 0,
    PHILOSOPHY_PSYCHOLOGY = 1,
    RELIGION = 2,
    SOCIAL_SCIENCE = 3,
    LANGUAGE = 4,
    SCIENCE = 5,
    TECHNOLOGY = 6,
    ART = 7,
    LITERATURE = 8,
    HISTORY_GEOGRAPHY = 9
  }
  
  export const CategoryNames: Record<Category, string> = {
    [Category.COMPUTER_SCIENCE]: '컴퓨터과학',
    [Category.PHILOSOPHY_PSYCHOLOGY]: '철학/심리학',
    [Category.RELIGION]: '종교',
    [Category.SOCIAL_SCIENCE]: '사회과학',
    [Category.LANGUAGE]: '언어',
    [Category.SCIENCE]: '과학',
    [Category.TECHNOLOGY]: '기술',
    [Category.ART]: '예술',
    [Category.LITERATURE]: '문학',
    [Category.HISTORY_GEOGRAPHY]: '역사/지리'
  }
  
  // 유틸리티 함수들
  export const CategoryUtils = {
    // number -> Category
    ofCode(code: number | null): Category | null {
      if (code === null) return null
      if (Object.values(Category).includes(code)) {
        return code as Category
      }
      throw new Error(`Invalid category code: ${code}`)
    },
  
    // Category -> string
    getCategoryName(category: Category | null): string | null {
      if (category === null) return null
      return CategoryNames[category]
    }
  }