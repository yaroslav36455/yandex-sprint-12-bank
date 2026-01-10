package by.tyv.transfer.controller;

import by.tyv.transfer.mapper.TransferMapper;
import by.tyv.transfer.model.dto.TransferRequestDto;
import by.tyv.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TransferController {
    private final TransferMapper transferMapper;
    private final TransferService transferService;

    @PostMapping("/transfer/{login}")
    public Mono<Void> postTransferMoney(@PathVariable("login") String login,
                                        @RequestBody TransferRequestDto operationTransfer) {
        return transferService.transferMoney(transferMapper.mapToTransferBO(operationTransfer).setSourceLogin(login));
    }
}
