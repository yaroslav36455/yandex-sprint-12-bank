package by.tyv.blocker.contoroller;

import by.tyv.blocker.model.dto.BlockerResponseDto;
import by.tyv.blocker.model.dto.OperationCashRequestDto;
import by.tyv.blocker.model.dto.OperationTransferRequestDto;
import by.tyv.blocker.service.BlockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class BlockerController {
    private final BlockerService blockerService;

    @PostMapping("/operations/available/cash")
    public Mono<BlockerResponseDto> availableCashOperation(@RequestBody OperationCashRequestDto operationCashRequestDto) {
        return blockerService.isAvailable(operationCashRequestDto)
                .map(BlockerResponseDto::new);
    }

    @PostMapping("/operations/available/transfer")
    public Mono<BlockerResponseDto> availableTransferOperation(@RequestBody OperationTransferRequestDto operationTransferRequestDto) {
        return blockerService.isAvailable(operationTransferRequestDto)
                .map(BlockerResponseDto::new);
    }
}
