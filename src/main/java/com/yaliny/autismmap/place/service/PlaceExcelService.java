package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.place.entity.*;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.DistrictRepository;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceExcelService {

    private final PlaceRepository placeRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public void uploadExcel(MultipartFile file) {

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<Place> places = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                Province province = provinceRepository.findById(
                    (long) row.getCell(11).getNumericCellValue()
                ).orElseThrow();

                District district = districtRepository.findById(
                    (long) row.getCell(12).getNumericCellValue()
                ).orElseThrow();

                log.info("low: {}, province: {}, district: {}", i, row.getCell(11).getNumericCellValue(), row.getCell(12).getNumericCellValue());

                Place place = Place.createPlace(
                    row.getCell(1).getStringCellValue(),
                    row.getCell(15).getStringCellValue(),
                    PlaceCategory.from(row.getCell(0).getStringCellValue()),
                    province,
                    district,
                    row.getCell(10).getStringCellValue(),
                    row.getCell(14).getNumericCellValue(),
                    row.getCell(13).getNumericCellValue(),
                    NoiseLevel.from((int) row.getCell(2).getNumericCellValue()),
                    row.getCell(5).getBooleanCellValue(),
                    row.getCell(6).getBooleanCellValue(),
                    false,
                    LightingLevel.from((int) row.getCell(3).getNumericCellValue()),
                    CrowdLevel.from((int) row.getCell(4).getNumericCellValue()),
                    getTimeCellValue(row.getCell(7)),
                    getTimeCellValue(row.getCell(8)),
                    row.getCell(9).getStringCellValue()
                );

                places.add(place);
            }

            placeRepository.saveAll(places);

        } catch (Exception e) {
            throw new RuntimeException("엑셀 업로드 실패!!", e);
        }
    }

    private String getTimeCellValue(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            if (DateUtil.isCellDateFormatted(cell)) {
                LocalTime time = cell.getLocalDateTimeCellValue().toLocalTime();
                return time.toString();
            }

        } else if (cell.getCellType() == CellType.STRING) {

            String value = cell.getStringCellValue();
            return parseKoreanTime(value).toString();
        }

        throw new IllegalArgumentException("지원하지 않는 시간 형식입니다.");
    }

    private LocalTime parseKoreanTime(String time) {

        time = time.trim();

        boolean isPM = time.startsWith("오후");
        boolean isAM = time.startsWith("오전");

        time = time.replace("오전", "")
            .replace("오후", "")
            .trim();

        LocalTime localTime = LocalTime.parse(time);

        if (isPM && localTime.getHour() != 12) {
            localTime = localTime.plusHours(12);
        }

        if (isAM && localTime.getHour() == 12) {
            localTime = localTime.minusHours(12);
        }

        return localTime;
    }
}
